package com.genai.extractor.application.service;

import com.genai.extractor.application.command.ExtractFileCommand;
import com.genai.extractor.application.command.ExtractFileTextCommand;
import com.genai.extractor.application.enums.ExtractType;
import com.genai.extractor.application.port.ExtractPort;
import com.genai.extractor.application.usecase.ExtractUseCase;
import com.genai.extractor.application.vo.ExtractContentVo;
import com.genai.extractor.domain.model.Document;
import com.genai.extractor.domain.model.FileDetail;
import com.genai.common.vo.UploadFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
}
