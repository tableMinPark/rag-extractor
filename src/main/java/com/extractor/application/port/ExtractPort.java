package com.extractor.application.port;

import com.extractor.domain.model.pattern.HwpxDocument;
import com.extractor.domain.model.FileDocument;
import com.extractor.domain.model.pattern.PdfDocument;

public interface ExtractPort {

    /**
     * 한글 문서 추출
     * @param fileDocument 원본 문서 정보
     */
    HwpxDocument extractHwpxDocumentPort(FileDocument fileDocument);

    /**
     * PDF 문서 추출
     * @param fileDocument 원본 문서 정보
     */
    PdfDocument extractPdfDocumentPort(FileDocument fileDocument);

    /**
     * 문서 텍스트 추출
     * @param fileDocument 원본 문서 정보
     */
    String extractDocumentPort(FileDocument fileDocument);
}
