package com.extractor.global.adapter.out;

import com.extractor.global.adapter.out.entity.LawContentEntity;
import com.extractor.global.adapter.out.entity.LawDocumentEntity;
import com.extractor.global.adapter.out.entity.LawLinkEntity;
import com.extractor.global.adapter.out.repository.LawContentRepository;
import com.extractor.global.adapter.out.repository.LawDocumentRepository;
import com.extractor.global.adapter.out.repository.LawLinkRepository;
import com.extractor.chunk.application.exception.NotFoundDocumentException;
import com.extractor.chunk.application.port.LawPersistencePort;
import com.extractor.chunk.domain.model.LawContent;
import com.extractor.chunk.domain.model.LawDocument;
import com.extractor.global.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private static final String LINK_TAG_PATTERN =
            "javascript:goLinkEx\\('([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?'\\)";

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    @Override
    public LawDocument getLawDocumentsPort(Long lawId) {

        // 법령 문서 엔티티 조회
        LawDocumentEntity lawDocumentEntity = lawDocumentRepository.findByLawId(lawId)
                .orElseThrow(NotFoundDocumentException::new);

        // 법령 본문 엔티티 목록 조회
        List<LawContentEntity> lawContentEntities = lawContentRepository.findByLawIdAndVersionOrderByArrange(
                lawDocumentEntity.getLawId(), lawDocumentEntity.getLatestVersion());

        // 연결 법령 본문 맵 (법령 본문 ID <-> 연결 법령 본문 목록)
        Map<Long, List<LawContent>> linkLawContentMap = new HashMap<>();
        for (LawContentEntity lawContentEntity : lawContentEntities) {
            // 법령 연결 정보 엔티티 목록 조회
            List<LawLinkEntity> lawLinkEntities = lawLinkRepository.findDistinctByLawContentIdAndVersion(
                    lawContentEntity.getLawContentId(), lawContentEntity.getVersion());

            // 연결 법령 본문 목록
            List<LawContent> linkLawContents = new ArrayList<>();
            for (LawLinkEntity lawLinkEntity : lawLinkEntities) {
                Pattern pattern = Pattern.compile(LINK_TAG_PATTERN);
                Matcher matcher = pattern.matcher(lawLinkEntity.getLinkTag());

                // LinkTag 필드 정규식 일치 여부 확인
                if (matcher.find()) {
                    String linkType = matcher.group(1);
                    String linkLawId = matcher.group(2);
                    String linkCode = matcher.group(3);
                    // String linkCodeEnd = matcher.group(4);
                    // String url = matcher.group(5);
                    // String articleCount = matcher.group(6);
                    // String articleCountEnd = matcher.group(7);

                    if (StringUtil.isNumber(linkLawId) && linkCode != null && !linkCode.isBlank()) {
                        switch (linkType) {
                            // 조 단위 연결 매핑
                            case "1" ->
                                    linkLawContents.addAll(lawContentRepository.findByLawIdAndLinkCodeAndVersion(Long.parseLong(linkLawId), linkCode, lawContentEntity.getVersion()).stream()
                                            .filter(linklawContentEntity -> !linklawContentEntity.getLawContentId().equals(lawContentEntity.getLawContentId()))
                                            .map(linkLawContentEntity -> LawContent.builder()
                                                    .lawContentId(linkLawContentEntity.getLawContentId())
                                                    .lawId(linkLawContentEntity.getLawId())
                                                    .version(linkLawContentEntity.getVersion())
                                                    .contentType(linkLawContentEntity.getContentType())
                                                    .categoryCode(linkLawContentEntity.getCategoryCode())
                                                    .arrange(linkLawContentEntity.getArrange())
                                                    .simpleTitle(linkLawContentEntity.getSimpleTitle())
                                                    .title(linkLawContentEntity.getTitle())
                                                    .content(StringUtil.removeHtml(linkLawContentEntity.getContent()))
                                                    .build())
                                            .toList());
                            // ???
                            case "2" -> {
                            }
                            // 법령 전체 연결 매핑
                            case "3" -> {
                            }
                        }
                    }
                }
            }

            linkLawContentMap.put(lawContentEntity.getLawContentId(), linkLawContents);
        }

        // 법령 본문 목록
        List<LawContent> lawContents = lawContentEntities.stream()
                .map(lawContentEntity -> LawContent.builder()
                        .lawContentId(lawContentEntity.getLawContentId())
                        .lawId(lawContentEntity.getLawId())
                        .version(lawContentEntity.getVersion())
                        .contentType(lawContentEntity.getContentType())
                        .categoryCode(lawContentEntity.getCategoryCode())
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
                .lawLinks(linkLawContentMap)
                .build();
    }
}
