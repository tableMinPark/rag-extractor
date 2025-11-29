package com.extractor.adapter.out;

import com.extractor.adapter.out.dto.manual.ManualAgendaDto;
import com.extractor.adapter.out.dto.manual.ManualContentDto;
import com.extractor.adapter.out.dto.manual.ManualTableContentDto;
import com.extractor.adapter.out.entity.LawContentEntity;
import com.extractor.adapter.out.entity.LawDocumentEntity;
import com.extractor.adapter.out.entity.LawLinkEntity;
import com.extractor.adapter.out.entity.ManualDocumentEntity;
import com.extractor.adapter.out.repository.LawContentRepository;
import com.extractor.adapter.out.repository.LawDocumentRepository;
import com.extractor.adapter.out.repository.LawLinkRepository;
import com.extractor.adapter.out.repository.ManualDocumentRepository;
import com.extractor.application.enums.ExtractType;
import com.extractor.application.exception.NotFoundException;
import com.extractor.application.port.DocumentReadPort;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.DocumentContent;
import com.extractor.domain.model.LawDocument;
import com.extractor.domain.vo.LawContentVo;
import com.extractor.domain.vo.LawLinkVo;
import com.extractor.global.utils.HtmlUtil;
import com.extractor.global.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DB 기반 법령 정보 조회 어댑터
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReadAdapter implements DocumentReadPort {

    private final ManualDocumentRepository manualDocumentRepository;
    private final LawDocumentRepository lawDocumentRepository;
    private final LawContentRepository lawContentRepository;
    private final LawLinkRepository lawLinkRepository;
    private final ObjectMapper objectMapper;

    private static final String LINK_TAG_PATTERN = "javascript:goLinkEx\\('([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d]+)?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?',\\s?'([a-zA-Z\\d])?'\\)";

    /**
     * 메뉴얼 문서 조회
     *
     * @param manualId 메뉴얼 ID
     */
    @Transactional
    @Override
    public Document getManualsPort(Long manualId, ExtractType extractType) {

        List<DocumentContent> documentContents = new ArrayList<>();

        ManualDocumentEntity manualDocumentEntity = manualDocumentRepository.findById(manualId)
                .orElseThrow(NotFoundException::new);

        try {
            // object mapper 역직렬화
            List<ManualTableContentDto> manualTableContentDto = objectMapper.readValue(manualDocumentEntity.getTableContent(), new TypeReference<List<ManualTableContentDto>>() {
            });
            List<ManualContentDto> manualContentDtos = objectMapper.readValue(manualDocumentEntity.getContent(), new TypeReference<List<ManualContentDto>>() {
            });

            // 목차 목록화
            if (!manualTableContentDto.isEmpty()) {
                Map<String, ManualAgendaDto> manualAgendaDtoMap = new HashMap<>();
                manualTableContentDto.getFirst().getContents().forEach(manualAgendaDto -> manualAgendaDtoMap.put(manualAgendaDto.getUuid(), manualAgendaDto));

                // 본문 매핑
                manualContentDtos.forEach(manualContentDto -> {
                    if (manualAgendaDtoMap.containsKey(manualContentDto.getUuid())) {
                        ManualAgendaDto manualAgendaDto = manualAgendaDtoMap.get(manualContentDto.getUuid());

                        String context = HtmlUtil.removeHtmlExceptTable(manualContentDto.getContent(), extractType);

                        documentContents.add(DocumentContent.builder()
                                .contentId(Long.parseLong(manualAgendaDto.getId()))
                                .compareText(context)
                                .title(manualAgendaDto.getTitle())
                                .simpleTitle(manualAgendaDto.getTitle())
                                .context(context)
                                .subDocumentContents(Collections.emptyList())
                                .type(DocumentContent.LineType.TEXT)
                                .build());
                    }
                });

                // contentId 기준 오름차순 정렬
                documentContents.sort((o1, o2) -> Math.toIntExact(o1.getContentId() - o2.getContentId()));
            }
        } catch (JsonProcessingException e) {
            throw new NotFoundException();
        }

        return new Document(manualDocumentEntity.getTitle(), documentContents);
    }

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    @Transactional
    @Override
    public LawDocument getLawsPort(Long lawId, ExtractType extractType) {

        // 법령 문서 엔티티 조회
        LawDocumentEntity lawDocumentEntity = lawDocumentRepository.findByLawId(lawId)
                .orElseThrow(NotFoundException::new);

        int version = lawDocumentEntity.getLatestVersion().orElseThrow(NotFoundException::new);

        // 법령 본문 엔티티 목록 조회
        List<LawContentEntity> lawContentEntities = lawContentRepository.findByLawIdAndVersionOrderByArrange(
                lawDocumentEntity.getLawId(), version);

        // 법령 본문 ID 목록
        List<Long> lawContentIds = lawContentEntities.stream()
                .map(LawContentEntity::getLawContentId)
                .toList();

        // 법령 연결 정보 엔티티 목록 조회
        List<LawLinkEntity> lawLinkEntities = lawLinkRepository.findDistinctByLawContentIdAndVersion(
                lawContentIds, version);

        // 법령 연결 정보 캐싱
        Map<Long, List<LawContentEntity>> lawContentEntityMap = new HashMap<>();
        for (LawLinkEntity lawLinkEntity : lawLinkEntities) {
            Matcher matcher = Pattern.compile(LINK_TAG_PATTERN).matcher(lawLinkEntity.getLinkTag());

            if (!matcher.find()) continue;
            if (matcher.group(1) == null || !"1".equals(matcher.group(1))) continue;
            if (matcher.group(3) == null || matcher.group(3).isBlank()) continue;
            if (StringUtil.isNumber(matcher.group(2))) continue;

            long targetLawId = Long.parseLong(matcher.group(2));

            // 법령 연결 정보 캐싱 처리
            if (!lawContentEntityMap.containsKey(targetLawId)) {
                lawContentEntityMap.put(targetLawId, lawContentRepository.findByLawIdOrderByVersionDesc(targetLawId));
            }
        }

        // 법령 연결 정보 생성 (타이틀, 본문 포함 객체 생성)
        Map<Long, List<LawLinkVo>> lawLinkMap = new HashMap<>();
        for (LawLinkEntity lawLinkEntity : lawLinkEntities) {
            Matcher matcher = Pattern.compile(LINK_TAG_PATTERN).matcher(lawLinkEntity.getLinkTag());

            if (!matcher.find()) continue;
            if (matcher.group(1) == null || !"1".equals(matcher.group(1))) continue;
            if (matcher.group(3) == null || matcher.group(3).isBlank()) continue;
            if (StringUtil.isNumber(matcher.group(2))) continue;

            long targetLawId = Long.parseLong(matcher.group(2));
            String linkCode = matcher.group(3);

            Optional<LawContentEntity> lawContentEntityOptional = findByTargetLawIdAndLinkCode(lawContentEntityMap.get(targetLawId), linkCode);

            if (lawContentEntityOptional.isPresent()) {
                LawContentEntity lawContentEntity = lawContentEntityOptional.get();
                if (!lawContentEntity.getLawContentId().equals(lawLinkEntity.getLawContentId())) {
                    List<LawLinkVo> linkLawContents = lawLinkMap.getOrDefault(lawLinkEntity.getLawContentId(), new ArrayList<>());
                    linkLawContents.add(LawLinkVo.builder()
                            .lawLinkId(lawLinkEntity.getLawLinkId())
                            .lawContentId(lawLinkEntity.getLawContentId())
                            .lawId(lawLinkEntity.getLawId())
                            .version(lawLinkEntity.getVersion())
                            .text(lawLinkEntity.getText())
                            .type(lawLinkEntity.getType())
                            .targetLawId(targetLawId)
                            .linkCode(linkCode)
                            .title(lawContentEntity.getTitle())
                            .content(replaceContent(HtmlUtil.removeHtmlExceptTable(lawContentEntity.getContent(), extractType)))
                            .build());
                    lawLinkMap.put(lawLinkEntity.getLawContentId(), linkLawContents);
                }
            }
        }

        // 법령 본문 목록
        List<LawContentVo> lawContentVos = new ArrayList<>();
        for (LawContentEntity lawContentEntity : lawContentEntities) {
            if ("addenda".equals(lawContentEntity.getCategoryCode())) {
                // TODO: 부칙인 경우 본문 정규식 기반 청킹 처리 필요
                lawContentVos.add(LawContentVo.builder()
                        .lawContentId(lawContentEntity.getLawContentId())
                        .lawId(lawContentEntity.getLawId())
                        .version(lawContentEntity.getVersion())
                        .contentType(lawContentEntity.getContentType())
                        .categoryCode(convertCategoryCode(lawContentEntity.getCategoryCode()))
                        .arrange(lawContentEntity.getArrange())
                        .simpleTitle(lawContentEntity.getSimpleTitle())
                        .title(lawContentEntity.getTitle())
                        .content(replaceContent(HtmlUtil.removeHtmlExceptTable(lawContentEntity.getContent(), extractType)))
                        .lawLinkVos(lawLinkMap.getOrDefault(lawContentEntity.getLawContentId(), Collections.emptyList()))
                        .build());
            } else {
                lawContentVos.add(LawContentVo.builder()
                        .lawContentId(lawContentEntity.getLawContentId())
                        .lawId(lawContentEntity.getLawId())
                        .version(lawContentEntity.getVersion())
                        .contentType(lawContentEntity.getContentType())
                        .categoryCode(convertCategoryCode(lawContentEntity.getCategoryCode()))
                        .arrange(lawContentEntity.getArrange())
                        .simpleTitle(lawContentEntity.getSimpleTitle())
                        .title(lawContentEntity.getTitle())
                        .content(replaceContent(HtmlUtil.removeHtmlExceptTable(lawContentEntity.getContent(), extractType)))
                        .lawLinkVos(lawLinkMap.getOrDefault(lawContentEntity.getLawContentId(), Collections.emptyList()))
                        .build());
            }
        }

        return LawDocument.builder()
                .lawId(lawDocumentEntity.getLawId())
                .lawName(lawDocumentEntity.getLawName())
                .lawContents(lawContentVos)
                .build();
    }

    /**
     * 법령 ID & LinkCode 기준 법령 본문 조회
     *
     * @param lawContentEntities 법령 본문 목록
     * @param linkCode           연결 코드
     * @return 법령 본문
     */
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

    /**
     * 텍스트 대치
     *
     * @param content 내용
     * @return 대치 이후 텍스트
     */
    private static String replaceContent(String content) {
        return content
                .replaceAll("삭\\s?제\\s?<\\d{4}.\\d{1,2}.\\d{1,2}>", "")
                .replaceAll("<개\\s?정\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                .replaceAll("<신\\s?설\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                ;
    }

    /**
     * 법령 구분 코드 대치
     *
     * @param code 구분 코드
     * @return 대치 구분 코드
     */
    private static String convertCategoryCode(String code) {
        return switch (code) {
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
