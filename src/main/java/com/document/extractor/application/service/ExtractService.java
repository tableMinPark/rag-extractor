package com.document.extractor.application.service;

import com.document.extractor.adapter.out.dto.ManualAgendaDto;
import com.document.extractor.adapter.out.dto.ManualContentDto;
import com.document.extractor.adapter.out.dto.ManualTableContentDto;
import com.document.extractor.application.command.ExtractFileCommand;
import com.document.extractor.application.command.ExtractFileTextCommand;
import com.document.extractor.application.command.ExtractLawCommand;
import com.document.extractor.application.command.ExtractManualCommand;
import com.document.extractor.application.enums.ExtractType;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.usecase.ExtractUseCase;
import com.document.extractor.application.vo.DocumentVo;
import com.document.extractor.application.vo.ExtractContentVo;
import com.document.extractor.domain.model.Document;
import com.document.extractor.domain.model.DocumentContent;
import com.document.extractor.domain.model.ExtractDocument;
import com.document.extractor.domain.model.FileDetail;
import com.document.global.utils.HtmlUtil;
import com.document.global.vo.UploadFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExtractService implements ExtractUseCase {

    private final ExtractPort extractPort;
    private final ObjectMapper objectMapper;

    /**
     * 문서 추출
     *
     * @param command 문서 추출 Command
     */
    @Override
    public List<ExtractContentVo> extractFileUseCase(ExtractFileCommand command) {

        UploadFile uploadFile = command.getFile();
        ExtractType extractType = ExtractType.find(command.getExtractType());

        Document document = extractPort.extractFilePort(FileDetail.builder()
                .originFileName(uploadFile.getOriginFileName())
                .fileName(uploadFile.getFileName())
                .url(uploadFile.getUrl())
                .filePath(uploadFile.getFilePath())
                .fileSize(uploadFile.getFileSize())
                .ext(uploadFile.getExt())
                .url(uploadFile.getUrl())
                .build(), extractType.getCode());

        return document.getDocumentContents().stream()
                .map(documentContent -> ExtractContentVo.builder()
                        .type(documentContent.getType().name())
                        .content(documentContent.getContext())
                        .build())
                .toList();
    }

    /**
     * 문서 텍스트 추출
     *
     * @param command 문서 텍스트 추출 Command
     */
    @Override
    public String extractFileTextUseCase(ExtractFileTextCommand command) {

        UploadFile uploadFile = command.getFile();

        return extractPort.extractTextPort(FileDetail.builder()
                .originFileName(uploadFile.getOriginFileName())
                .fileName(uploadFile.getFileName())
                .url(uploadFile.getUrl())
                .filePath(uploadFile.getFilePath())
                .fileSize(uploadFile.getFileSize())
                .ext(uploadFile.getExt())
                .url(uploadFile.getUrl())
                .build());
    }

    /**
     * 법령 문서 추출
     *
     * @param command 법령 문서 추출 Command
     * @return 법령 문서 추출
     */
    @Override
    public DocumentVo extractLawUseCase(ExtractLawCommand command) {
        List<DocumentContent> documentContents = new ArrayList<>();

        ExtractDocument lawResponse = extractPort.extractLawPort(command.getLawId());

        String title = lawResponse.getResult().getRows().getFirst().getFields().get("LAWNAME");
        String lawId = lawResponse.getResult().getRows().getFirst().getFields().get("SEQ");
        String lawHistory = extractPort.extractLawHistoryPort(lawId).getResult().getRows().getFirst().getFields().get("SEQ_HISTORY");

        // TODO: 법령 연결 정보 목록 조회 및 연결 본문 MAP 생성

        // 법령 본문 목록 조회
        extractPort.extractLawContentPort(lawId, lawHistory).getResult().getRows().stream()
                .sorted((o1, o2) -> Math.toIntExact(Long.parseLong(o1.getFields().get("ARRANGE")) - Long.parseLong(o2.getFields().get("ARRANGE"))))
                .forEach(row -> documentContents.add(DocumentContent.builder()
                        .contentId(String.valueOf(documentContents.size()))
                        .compareText(row.getFields().get("GUBUN"))
                        .title(row.getFields().get("TITLE"))
                        .simpleTitle(row.getFields().get("TITLE"))
                        .context(HtmlUtil.removeHtml(row.getFields().get("CONTENTS")))
                        .subDocumentContents(Collections.emptyList())
                        .type(DocumentContent.LineType.TEXT)
                        .build()));

        // contentId 기준 오름차순 정렬
        documentContents.sort(((o1, o2) -> Math.toIntExact(Long.parseLong(o1.getContentId()) - Long.parseLong(o2.getContentId()))));

        return DocumentVo.of(new Document(title, documentContents));

    }

    /**
     * 메뉴얼 문서 추출
     *
     * @param command 메뉴얼 문서 추출 Command
     * @return 메뉴얼 문서 추출
     */
    @Override
    public DocumentVo extractManualUseCase(ExtractManualCommand command) {

        ExtractDocument extractDocument = extractPort.extractManualPort(command.getManualId());

        try {
            List<DocumentContent> documentContents = new ArrayList<>();

            String title = extractDocument.getResult().getRows().getFirst().getFields().get("TITLE");
            String tableContent = extractDocument.getResult().getRows().getFirst().getFields().get("TABLE_CONTENT");
            String content = extractDocument.getResult().getRows().getFirst().getFields().get("CONTENT");

            List<ManualTableContentDto> manualTableContentDtos = objectMapper.readValue(tableContent, new TypeReference<List<ManualTableContentDto>>() {
            });
            List<ManualContentDto> manualContentDtos = objectMapper.readValue(content, new TypeReference<List<ManualContentDto>>() {
            });

            // 목차 목록화
            if (!manualTableContentDtos.isEmpty()) {
                Map<String, ManualAgendaDto> manualAgendaDtoMap = new HashMap<>();

                for (ManualTableContentDto manualTableContentDto : manualTableContentDtos) {
                    for (ManualAgendaDto manualAgendaDto : manualTableContentDto.getContents()) {
                        ManualAgendaDto.toList(manualAgendaDto).forEach(agendaDto -> {
                            manualAgendaDtoMap.put(agendaDto.getUuid(), agendaDto);
                        });
                    }
                }
                // 본문 매핑
                manualContentDtos.forEach(manualContentDto -> {
                    if (manualAgendaDtoMap.containsKey(manualContentDto.getUuid())) {
                        ManualAgendaDto manualAgendaDto = manualAgendaDtoMap.get(manualContentDto.getUuid());

                        String context = manualContentDto.getContent();

                        context = HtmlUtil.removeHtmlExceptTable(context);

                        // 마크 다운 타입인 경우 표 변환
                        if (ExtractType.MARK_DOWN.equals(command.getExtractType())) {
                            context = HtmlUtil.convertTableHtmlToMarkdown(context);
                        } else {
                            context = context.replaceAll("<td>([<br>]|\\n)+?</td>", "<td></td>");
                        }

                        documentContents.add(DocumentContent.builder()
                                .contentId(manualAgendaDto.getId())
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
                documentContents.sort(Comparator.comparing(DocumentContent::getContentId));
            }

            return DocumentVo.of(new Document(title, documentContents));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new NotFoundException("원격 메뉴얼 문서");
        }
    }
}
