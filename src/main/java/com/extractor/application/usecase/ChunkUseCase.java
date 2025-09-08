package com.extractor.application.usecase;

import com.extractor.application.vo.PassageDocumentVo;
import com.extractor.domain.vo.document.FileDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;

import java.util.List;

public interface ChunkUseCase {

    /**
     * 한글 문서 청킹
     * @param fileDocumentVo 원본 문서 정보
     */
    List<PassageDocumentVo> chunkHwpxDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * PDF 문서 청킹
     * @param fileDocumentVo 원본 문서 정보
     */
    List<PassageDocumentVo> chunkPdfDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * 법령 문서 청킹
     * @param lawIds 법령 ID 목록
     */
    List<PassageDocumentVo> chunkLawDocumentUseCase(List<Long> lawIds);
}
