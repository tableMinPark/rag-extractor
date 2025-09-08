package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.usecase.ExtractUseCase;
import com.extractor.domain.model.pattern.HwpxDocument;
import com.extractor.domain.model.FileDocument;
import com.extractor.domain.model.pattern.PdfDocument;
import com.extractor.application.vo.DocumentLineVo;
import com.extractor.application.vo.ExtractDocumentVo;
import com.extractor.domain.vo.document.FileDocumentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractService implements ExtractUseCase {

    private final ExtractPort extractPort;

    private final FilePort filePort;

    /**
     * 한글 문서 추출
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public ExtractDocumentVo extractHwpxDocumentUseCase(FileDocumentVo fileDocumentVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(fileDocument);
            hwpxDocument.extract();
            return ExtractDocumentVo.builder()
                    .docId(hwpxDocument.getDocId())
                    .name(hwpxDocument.getName())
                    .extension(hwpxDocument.getExtension())
                    .lines(hwpxDocument.getLines().stream()
                            .map(line -> DocumentLineVo.builder()
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
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public ExtractDocumentVo extractPdfDocumentUseCase(FileDocumentVo fileDocumentVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            PdfDocument pdfDocument = extractPort.extractPdfDocumentPort(fileDocument);
            pdfDocument.extract();
            return ExtractDocumentVo.builder()
                    .docId(pdfDocument.getDocId())
                    .name(pdfDocument.getName())
                    .extension(pdfDocument.getExtension())
                    .lines(pdfDocument.getLines().stream()
                            .map(line -> DocumentLineVo.builder()
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
