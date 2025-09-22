package com.extractor.application.usecase;

import com.extractor.application.vo.ChunkDocumentVo;
import com.extractor.domain.vo.FileDocumentVo;
import com.extractor.domain.vo.ChunkPatternVo;

import java.util.List;

public interface ChunkUseCase {

    /**
     * 한글 문서 청킹
     *
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    ChunkDocumentVo chunkHwpxDocumentUseCase(String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * PDF 문서 청킹
     *
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    ChunkDocumentVo chunkPdfDocumentUseCase(String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * 법령 문서 청킹
     *
     * @param categoryCode   카테고리 코드
     * @param lawId          법령 ID
     * @param chunkPatternVo 청킹 패턴 정보
     */
    ChunkDocumentVo chunkLawDocumentUseCase(String categoryCode, Long lawId, ChunkPatternVo chunkPatternVo);
}
