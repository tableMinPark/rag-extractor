package com.extractor.application.usecase;

import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;

public interface ChunkHwpxUseCase {

    /**
     * 한글 문서 청킹
     * @param originalDocumentVo 원본 문서 정보
     */
    HwpxDocument chunkHwpxDocument(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo);
}
