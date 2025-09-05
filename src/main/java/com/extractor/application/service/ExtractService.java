package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.usecase.ExtractUseCase;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.application.vo.DocumentLineVo;
import com.extractor.application.vo.ExtractDocumentVo;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractService implements ExtractUseCase {

    private final ExtractPort extractPort;

    private final FilePort filePort;

    /**
     * 한글 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public ExtractDocumentVo extractHwpxDocumentUseCase(OriginalDocumentVo originalDocumentVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(originalDocument);
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
            filePort.clearFilePort(originalDocument);
        }
    }

    /**
     * PDf 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public ExtractDocumentVo extractPdfDocumentUseCase(OriginalDocumentVo originalDocumentVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            PdfDocument pdfDocument = extractPort.extractPdfDocumentPort(originalDocument);
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
            filePort.clearFilePort(originalDocument);
        }
    }

    /**
     * 문서 텍스트 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public String extractDocumentUseCase(OriginalDocumentVo originalDocumentVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            return extractPort.extractDocumentPort(originalDocument);
        } finally {
            // 파일 삭제
            filePort.clearFilePort(originalDocument);
        }
    }
}
