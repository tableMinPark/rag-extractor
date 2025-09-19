package com.extractor.application.usecase;

import com.extractor.application.vo.ExtractDocumentVo;
import com.extractor.domain.vo.FileDocumentVo;

public interface ExtractUseCase {

    /**
     * 한글 문서 추출
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    ExtractDocumentVo extractHwpxDocumentUseCase(FileDocumentVo fileDocumentVo);

    /**
     * PDf 문서 추출
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    ExtractDocumentVo extractPdfDocumentUseCase(FileDocumentVo fileDocumentVo);

    /**
     * 문서 텍스트 추출
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    String extractDocumentUseCase(FileDocumentVo fileDocumentVo);
}