package com.extractor.adapter.out;

import com.extractor.adapter.out.dto.ManualAgendaDto;
import com.extractor.adapter.out.dto.ManualContentDto;
import com.extractor.adapter.out.dto.ManualTableContentDto;
import com.extractor.adapter.out.entity.ManualDocumentEntity;
import com.extractor.adapter.out.repository.ManualDocumentRepository;
import com.extractor.application.exception.NotFoundDocumentException;
import com.extractor.application.port.ManualPersistencePort;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.DocumentContent;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualPersistenceAdapter implements ManualPersistencePort {

    private final ManualDocumentRepository manualDocumentRepository;

    private final ObjectMapper objectMapper;

    /**
     * 메뉴얼 문서 조회
     *
     * @param manualId 메뉴얼 ID
     */
    @Override
    public Document getManualDocumentsPort(Long manualId) {

        List<DocumentContent> documentContents = new ArrayList<>();

        ManualDocumentEntity manualDocumentEntity = manualDocumentRepository.findById(manualId)
                .orElseThrow(NotFoundDocumentException::new);


        try {
            // object mapper 역직렬화
            List<ManualTableContentDto> manualTableContentDto = objectMapper.readValue(manualDocumentEntity.getTableContent(), new TypeReference<List<ManualTableContentDto>>() {});
            List<ManualContentDto> manualContentDtos = objectMapper.readValue(manualDocumentEntity.getContent(), new TypeReference<List<ManualContentDto>>() {});

            // 목차 목록화
            if (!manualTableContentDto.isEmpty()) {
                Map<String, ManualAgendaDto> manualAgendaDtoMap = new HashMap<>();
                manualTableContentDto.getFirst().getContents().forEach(manualAgendaDto ->
                    manualAgendaDtoMap.put(manualAgendaDto.getUuid(), manualAgendaDto));

                // 본문 매핑
                manualContentDtos.forEach(manualContentDto -> {
                    if (manualAgendaDtoMap.containsKey(manualContentDto.getUuid())) {
                        ManualAgendaDto manualAgendaDto = manualAgendaDtoMap.get(manualContentDto.getUuid());

                        String context = StringUtil.removeHtml(manualContentDto.getContent());

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
            throw new NotFoundDocumentException();
        }

        documentContents.forEach(documentContent -> {
            // TODO: HTML 태그 제거 로직 추가 필요
            // TODO: 1 depth 의 테이블 벗기고 -> 이후 table 태그 제외하고 모든 태그 벗기기 순으로 진행할 예정
            log.info("\ntitle: {}\ncontent: {}", documentContent.getTitle(), documentContent.getContext());//.substring(0, Math.min(100, documentContent.getContext().length())));
        });

        return new Document(manualDocumentEntity.getTitle(), FileExtension.DATABASE, null, documentContents);
    }
}
