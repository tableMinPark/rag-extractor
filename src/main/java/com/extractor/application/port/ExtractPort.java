package com.extractor.application.port;

import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.PdfDocument;

public interface ExtractPort {

    /**
     * 한글 문서 추출
     * @param originalDocument 원본 문서 정보
     */
    HwpxDocument extractHwpxDocumentPort(OriginalDocument originalDocument);

    /**
     * PDF 문서 추출
     * @param originalDocument 원본 문서 정보
     */
    PdfDocument extractPdfDocumentPort(OriginalDocument originalDocument);

    /**
     * 문서 텍스트 추출
     * @param originalDocument 원본 문서 정보
     */
    String extractDocumentPort(OriginalDocument originalDocument);
}
