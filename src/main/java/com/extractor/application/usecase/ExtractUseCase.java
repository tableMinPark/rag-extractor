package com.extractor.application.usecase;

import com.extractor.application.vo.ExtractVo;
import com.extractor.application.vo.FileVo;
import com.extractor.global.enums.ExtractType;

public interface ExtractUseCase {

    /**
     * 한글 문서 추출
     *
     * @param fileVo 원본 문서 정보
     */
    ExtractVo extractHwpxDocumentUseCase(FileVo fileVo, ExtractType extractType);

    /**
     * PDf 문서 추출
     *
     * @param fileVo 원본 문서 정보
     */
    ExtractVo extractPdfDocumentUseCase(FileVo fileVo);

    /**
     * 문서 텍스트 추출
     *
     * @param fileVo 원본 문서 정보
     */
    String extractDocumentUseCase(FileVo fileVo);
}