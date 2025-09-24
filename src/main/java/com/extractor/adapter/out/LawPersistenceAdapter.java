package com.extractor.adapter.out;

import com.extractor.adapter.out.entity.LawContentEntity;
import com.extractor.adapter.out.entity.LawDocumentEntity;
import com.extractor.adapter.out.entity.LawLinkEntity;
import com.extractor.adapter.out.repository.LawContentRepository;
import com.extractor.adapter.out.repository.LawDocumentRepository;
import com.extractor.adapter.out.repository.LawLinkRepository;
import com.extractor.application.exception.NotFoundDocumentException;
import com.extractor.application.port.LawPersistencePort;
import com.extractor.domain.model.LawContent;
import com.extractor.domain.model.LawDocument;
import com.extractor.domain.model.LawLink;
import com.extractor.global.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawPersistenceAdapter implements LawPersistencePort {

    private final LawDocumentRepository lawDocumentRepository;

    private final LawContentRepository lawContentRepository;

    private final LawLinkRepository lawLinkRepository;

    private static final String LINK_TAG_PATTERN = "javascript:goLinkEx\\('([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?'\\)";

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    @Override
    @Transactional
    public LawDocument getLawDocumentsPort(Long lawId) {

        // 법령 문서 엔티티 조회
        LawDocumentEntity lawDocumentEntity = lawDocumentRepository.findByLawId(lawId)
                .orElseThrow(NotFoundDocumentException::new);

        // 법령 본문 엔티티 목록 조회
        List<LawContentEntity> lawContentEntities = lawContentRepository.findByLawIdAndVersionOrderByArrange(
                lawDocumentEntity.getLawId(), lawDocumentEntity.getLatestVersion());

        // 법령 본문 ID 목록
        List<Long> lawContentIds = lawContentEntities.stream()
                .map(LawContentEntity::getLawContentId)
                .toList();

        // 법령 연결 정보 엔티티 목록 조회
        List<LawLinkEntity> lawLinkEntities = lawLinkRepository.findDistinctByLawContentIdAndVersion(
                lawContentIds, lawDocumentEntity.getLatestVersion());

        // 법령 연결 정보 캐싱
        Map<Long, List<LawContentEntity>> lawContentEntityMap = new HashMap<>();
        for (LawLinkEntity lawLinkEntity : lawLinkEntities) {
            Matcher matcher = Pattern.compile(LINK_TAG_PATTERN).matcher(lawLinkEntity.getLinkTag());

            if (!matcher.find()) continue;
            if (matcher.group(1) == null || !"1".equals(matcher.group(1))) continue;
            if (matcher.group(3) == null || matcher.group(3).isBlank()) continue;
            if (!StringUtil.isNumber(matcher.group(2))) continue;

            long targetLawId = Long.parseLong(matcher.group(2));

            // 법령 연결 정보 중복 제거
            if (!lawContentEntityMap.containsKey(targetLawId)) {
                lawContentEntityMap.put(targetLawId, lawContentRepository.findByLawIdOrderByVersionDesc(targetLawId));
            }
        }

        // 법령 연결 정보 생성 (타이틀, 본문 포함 객체 생성)
        Map<Long, List<LawLink>> lawLinkMap = new HashMap<>();
        for (LawLinkEntity lawLinkEntity : lawLinkEntities) {
            Matcher matcher = Pattern.compile(LINK_TAG_PATTERN).matcher(lawLinkEntity.getLinkTag());

            if (!matcher.find()) continue;
            if (matcher.group(1) == null || !"1".equals(matcher.group(1))) continue;
            if (matcher.group(3) == null || matcher.group(3).isBlank()) continue;
            if (!StringUtil.isNumber(matcher.group(2))) continue;

            long targetLawId = Long.parseLong(matcher.group(2));
            String linkCode = matcher.group(3);

            Optional<LawContentEntity> lawContentEntityOptional = findByTargetLawIdAndLinkCode(lawContentEntityMap.get(targetLawId), linkCode);

            if (lawContentEntityOptional.isPresent()) {
                LawContentEntity lawContentEntity = lawContentEntityOptional.get();

                if (!lawContentEntity.getLawContentId().equals(lawLinkEntity.getLawContentId())) {
                    List<LawLink> linkLawContents = lawLinkMap.getOrDefault(lawLinkEntity.getLawContentId(), new ArrayList<>());
                    linkLawContents.add(LawLink.builder()
                            .lawLinkId(lawLinkEntity.getLawLinkId())
                            .lawContentId(lawLinkEntity.getLawContentId())
                            .lawId(lawLinkEntity.getLawId())
                            .version(lawLinkEntity.getVersion())
                            .text(lawLinkEntity.getText())
                            .type(lawLinkEntity.getType())
                            .targetLawId(targetLawId)
                            .linkCode(linkCode)
                            .title(lawContentEntity.getTitle())
                            .content(replaceContent(StringUtil.removeHtml(lawContentEntity.getContent())))
                            .build());
                    lawLinkMap.put(lawLinkEntity.getLawContentId(), linkLawContents);
                }
            }
        }

        // 법령 본문 목록
        List<LawContent> lawContents = lawContentEntities.stream()
                .map(lawContentEntity -> LawContent.builder()
                        .lawContentId(lawContentEntity.getLawContentId())
                        .lawId(lawContentEntity.getLawId())
                        .version(lawContentEntity.getVersion())
                        .contentType(lawContentEntity.getContentType())
                        .categoryCode(convertCategoryCode(lawContentEntity.getCategoryCode()))
                        .arrange(lawContentEntity.getArrange())
                        .simpleTitle(lawContentEntity.getSimpleTitle())
                        .title(lawContentEntity.getTitle())
                        .content(replaceContent(StringUtil.removeHtml(lawContentEntity.getContent())))
                        .build())
                .collect(Collectors.toList());

        return LawDocument.builder()
                .lawId(lawDocumentEntity.getLawId())
                .lawName(lawDocumentEntity.getLawName())
                .lawContents(lawContents)
                .lawLinks(lawLinkMap)
                .build();
    }

    private Optional<LawContentEntity> findByTargetLawIdAndLinkCode(List<LawContentEntity> lawContentEntities, String linkCode) {

        if (lawContentEntities == null || lawContentEntities.isEmpty()) {
            return Optional.empty();
        }

        for (LawContentEntity lawContentEntity : lawContentEntities) {
            if (lawContentEntity.getLinkCode().equals(linkCode)) {
                return Optional.of(lawContentEntity);
            }
        }

        return Optional.empty();
    }

    private static String replaceContent(String content) {
        return content
                .replaceAll("삭\\s?제\\s?<\\d{4}.\\d{1,2}.\\d{1,2}>", "")
                .replaceAll("<개\\s?정\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                .replaceAll("<신\\s?설\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                .replaceAll("<신\\s?설\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                ;
    }

    private static String convertCategoryCode(String categoryCode) {
        return switch (categoryCode) {
            case "lawname" -> "NAME";
            case "addenda" -> "BUCHICK";
            case "history" -> "HISTORY";
            case "part" -> "PYUN";
            case "chapter" -> "JANG";
            case "section" -> "JUL";
            case "subsection" -> "GWAN";
            case "article" -> "JO";
            default -> "";
        };
    }
}
