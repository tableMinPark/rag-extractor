package com.extractor.application.port;

import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;

public interface ExtractPort {

    /**
     * 한글 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    HwpxDocument extractHwpxDocumentPort(OriginalDocumentVo originalDocumentVo);

    /**
     * PDF 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    PdfDocument extractPdfDocumentPort(OriginalDocumentVo originalDocumentVo);
}
