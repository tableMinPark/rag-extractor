package com.document.extractor.application.service;

import com.document.extractor.application.command.ExtractFileCommand;
import com.document.extractor.application.command.ExtractFileTextCommand;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.usecase.ExtractUseCase;
import com.document.extractor.application.vo.ExtractContentVo;
import com.document.global.vo.UploadFile;
import com.document.extractor.domain.model.Document;
import com.document.extractor.domain.model.FileDetail;
import com.document.extractor.application.enums.ExtractType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractService implements ExtractUseCase {

    private final ExtractPort extractPort;

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
