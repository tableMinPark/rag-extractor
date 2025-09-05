package com.extractor.application.usecase;

import com.extractor.application.vo.ExtractDocumentVo;
import com.extractor.domain.vo.document.OriginalDocumentVo;

public interface ExtractUseCase {

    /**
     * 한글 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    ExtractDocumentVo extractHwpxDocumentUseCase(OriginalDocumentVo originalDocumentVo);

    /**
     * PDf 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    ExtractDocumentVo extractPdfDocumentUseCase(OriginalDocumentVo originalDocumentVo);

    /**
     * 문서 텍스트 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    String extractDocumentUseCase(OriginalDocumentVo originalDocumentVo);
}
