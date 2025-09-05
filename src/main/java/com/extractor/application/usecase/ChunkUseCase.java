package com.extractor.application.usecase;

import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.application.vo.PassageDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;

import java.util.List;

public interface ChunkUseCase {

    /**
     * 한글 문서 청킹
     * @param originalDocumentVo 원본 문서 정보
     */
    List<PassageDocumentVo> chunkHwpxDocumentUseCase(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo);

    /**
     * PDF 문서 청킹
     * @param originalDocumentVo 원본 문서 정보
     */
    List<PassageDocumentVo> chunkPdfDocumentUseCase(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo);
}
