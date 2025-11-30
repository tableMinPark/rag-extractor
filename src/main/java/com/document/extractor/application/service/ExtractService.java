package com.document.extractor.application.service;

import com.document.extractor.application.command.ExtractFileCommand;
import com.document.extractor.application.command.ExtractFileTextCommand;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.usecase.ExtractUseCase;
import com.document.extractor.application.vo.ExtractContentVo;
import com.document.extractor.application.vo.FileVo;
import com.document.extractor.domain.model.Document;
import com.document.extractor.domain.model.FileDetail;
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

        FileVo fileVo = command.getFile();

        Document document = extractPort.extractFilePort(FileDetail.builder()
                .originalFileName(fileVo.getOriginFileName())
                .fileName(fileVo.getFileName())
                .url(fileVo.getUrl())
                .filePath(fileVo.getFilePath())
                .fileSize(fileVo.getFileSize())
                .ext(fileVo.getExt())
                .url(fileVo.getUrl())
                .build(), command.getExtractType());

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

        FileVo fileVo = command.getFile();

        return extractPort.extractTextPort(FileDetail.builder()
                .originalFileName(fileVo.getOriginFileName())
                .fileName(fileVo.getFileName())
                .url(fileVo.getUrl())
                .filePath(fileVo.getFilePath())
                .fileSize(fileVo.getFileSize())
                .ext(fileVo.getExt())
                .url(fileVo.getUrl())
                .build());
    }
}
