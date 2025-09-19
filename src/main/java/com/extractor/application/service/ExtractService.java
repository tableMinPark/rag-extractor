package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.usecase.ExtractUseCase;
import com.extractor.application.vo.ExtractContentVo;
import com.extractor.application.vo.ExtractDocumentVo;
import com.extractor.domain.model.FileDocument;
import com.extractor.domain.model.ExtractHwpxDocument;
import com.extractor.domain.model.ExtractPdfDocument;
import com.extractor.domain.vo.FileDocumentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractService implements ExtractUseCase {

    private final ExtractPort extractPort;

    private final FilePort filePort;

    /**
     * 한글 문서 추출
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public ExtractDocumentVo extractHwpxDocumentUseCase(FileDocumentVo fileDocumentVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractHwpxDocument extractHwpxDocument = extractPort.extractHwpxDocumentPort(fileDocument);
            extractHwpxDocument.extract();
            return ExtractDocumentVo.builder()
                    .name(extractHwpxDocument.getName())
                    .extension(extractHwpxDocument.getExtension())
                    .extractContents(extractHwpxDocument.getExtractContents().stream()
                            .map(line -> ExtractContentVo.builder()
                                    .type(line.getType().name())
                                    .content(line.getContent())
                                    .build())
                            .toList())
                    .build();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * PDf 문서 추출
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public ExtractDocumentVo extractPdfDocumentUseCase(FileDocumentVo fileDocumentVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractPdfDocument extractPdfDocument = extractPort.extractPdfDocumentPort(fileDocument);
            extractPdfDocument.extract();
            return ExtractDocumentVo.builder()
                    .name(extractPdfDocument.getName())
                    .extension(extractPdfDocument.getExtension())
                    .extractContents(extractPdfDocument.getExtractContents().stream()
                            .map(line -> ExtractContentVo.builder()
                                    .type(line.getType().name())
                                    .content(line.getContent())
                                    .build())
                            .toList())
                    .build();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * 문서 텍스트 추출
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public String extractDocumentUseCase(FileDocumentVo fileDocumentVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            return extractPort.extractDocumentPort(fileDocument);
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }
}
