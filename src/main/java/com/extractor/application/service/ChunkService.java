package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChunkService implements ChunkUseCase {

    private final ExtractPort extractPort;

    private final FilePort filePort;

    /**
     * 한글 문서 청킹
     * @param originalDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    @Override
    public HwpxDocument chunkHwpxDocument(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(originalDocument);
            hwpxDocument.extract();
//            hwpxDocument.topDownSelectPassage(chunkPatternVo);    // TODO: 테스트 이후 변경 예정
            hwpxDocument.bundleSelectPassage(chunkPatternVo);       // TODO: 테스트 이후 변경 예정

            return hwpxDocument;

        } finally {
            // 파일 삭제
            filePort.clearFilePort(originalDocument);
        }
    }

    /**
     * PDF 문서 청킹
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public PdfDocument chunkPdfDocument(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            PdfDocument pdfDocument = extractPort.extractPdfDocumentPort(originalDocument);
            pdfDocument.extract();
            pdfDocument.topDownSelectPassage(chunkPatternVo);

            return pdfDocument;

        } finally {
            // 파일 삭제
            filePort.clearFilePort(originalDocument);
        }
    }
}
