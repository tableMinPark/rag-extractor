package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.domain.model.*;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.application.vo.PassageDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<PassageDocumentVo> chunkHwpxDocumentUseCase(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(originalDocument);
            hwpxDocument.extract();
            return this.chunkDocumentUseCase(hwpxDocument, chunkPatternVo).stream()
                    .map(passageDocument -> PassageDocumentVo.builder()
                            .docId(passageDocument.getDocId())
                            .depth(passageDocument.getDepth())
                            .tokenSize(passageDocument.getTokenSize())
                            .fullTitle(passageDocument.getFullTitle())
                            .titles(passageDocument.getTitles())
                            .content(passageDocument.getContent())
                            .build())
                    .toList();
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
    public List<PassageDocumentVo> chunkPdfDocumentUseCase(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        OriginalDocument originalDocument = filePort.uploadFilePort(originalDocumentVo);

        try {
            PdfDocument pdfDocument = extractPort.extractPdfDocumentPort(originalDocument);
            pdfDocument.extract();
            return this.chunkDocumentUseCase(pdfDocument, chunkPatternVo).stream()
                    .map(passageDocument -> PassageDocumentVo.builder()
                            .docId(passageDocument.getDocId())
                            .depth(passageDocument.getDepth())
                            .tokenSize(passageDocument.getTokenSize())
                            .fullTitle(passageDocument.getFullTitle())
                            .titles(passageDocument.getTitles())
                            .content(passageDocument.getContent())
                            .build())
                    .toList();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(originalDocument);
        }
    }

    /**
     * 문서 청킹
     * @param extractDocument 추출 문서
     * @param chunkPatternVo 청킹 패턴
     */
    private List<PassageDocument> chunkDocumentUseCase(ExtractDocument extractDocument, ChunkPatternVo chunkPatternVo) {

        PassageDocument passageDocument = new PassageDocument(
                extractDocument.getDocId(),
                chunkPatternVo.getPatterns().size(),
                extractDocument.getLines());

        return passageDocument.chunk(
                chunkPatternVo.getPatterns(), chunkPatternVo.getStopPatterns());
    }
}
