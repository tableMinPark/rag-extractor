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
import com.extractor.global.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // 법령 연결 정보 목록 (법령 본문 ID, 버전, 법령 ID, 링크 코드)
        List<LawLinkInfoVo> lawLinkInfos = new ArrayList<>();
        for (LawLinkEntity lawLinkEntity : lawLinkEntities) {
            Matcher matcher = Pattern.compile(LINK_TAG_PATTERN).matcher(lawLinkEntity.getLinkTag());

            if (!matcher.find()) continue;
            if (matcher.group(1) == null || !"1".equals(matcher.group(1))) continue;
            if (matcher.group(3) == null || matcher.group(3).isBlank()) continue;
            if (!StringUtil.isNumber(matcher.group(2))) continue;

            lawLinkInfos.add(new LawLinkInfoVo(
                    lawLinkEntity.getLawContentId(), lawLinkEntity.getVersion(), Long.parseLong(matcher.group(2)), matcher.group(3)));
        }

        Map<Long, List<LawContent>> linkLawContentsMap = new HashMap<>();
        for (LawLinkInfoVo lawLinkInfo : lawLinkInfos) {
            List<LawContent> linkLawContents = linkLawContentsMap.getOrDefault(lawLinkInfo.getLawContentId(), new ArrayList<>());
            linkLawContents.addAll(lawContentRepository.findTopByLawIdAndLinkCodeOrderByVersionDesc(lawLinkInfo.getLawId(), lawLinkInfo.getLinkCode()).stream()
                            .filter(linklawContentEntity -> !linklawContentEntity.getLawContentId().equals(lawLinkInfo.getLawContentId()))
                            .map(linkLawContentEntity -> LawContent.builder()
                                    .lawContentId(linkLawContentEntity.getLawContentId())
                                    .lawId(linkLawContentEntity.getLawId())
                                    .version(linkLawContentEntity.getVersion())
                                    .contentType(linkLawContentEntity.getContentType())
                                    .categoryCode(convertCategoryCode(linkLawContentEntity.getCategoryCode()))
                                    .arrange(linkLawContentEntity.getArrange())
                                    .simpleTitle(linkLawContentEntity.getSimpleTitle())
                                    .title(linkLawContentEntity.getTitle())
                                    .content(StringUtil.removeHtml(linkLawContentEntity.getContent()))
                                    .build())
                            .toList());

            linkLawContentsMap.put(lawLinkInfo.getLawContentId(), linkLawContents);
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
                        .content(StringUtil.removeHtml(lawContentEntity.getContent()))
                        .build())
                .collect(Collectors.toList());

        return LawDocument.builder()
                .lawId(lawDocumentEntity.getLawId())
                .lawName(lawDocumentEntity.getLawName())
                .lawContents(lawContents)
                .lawLinks(linkLawContentsMap)
                .build();
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
