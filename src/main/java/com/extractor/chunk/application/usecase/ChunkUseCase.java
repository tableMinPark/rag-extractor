package com.extractor.chunk.application.usecase;

import com.extractor.chunk.application.vo.ChunkVo;
import com.extractor.extract.domain.vo.FileDocumentVo;
import com.extractor.chunk.domain.vo.ChunkPatternVo;

import java.util.List;

public interface ChunkUseCase {

    /**
     * 한글 문서 청킹
     *
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    List<ChunkVo> chunkHwpxDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * PDF 문서 청킹
     *
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    List<ChunkVo> chunkPdfDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * 법령 문서 청킹
     *
     * @param lawId          법령 ID
     * @param chunkPatternVo 청킹 패턴 정보
     */
    List<ChunkVo> chunkLawDocumentUseCase(Long lawId, ChunkPatternVo chunkPatternVo);
}
