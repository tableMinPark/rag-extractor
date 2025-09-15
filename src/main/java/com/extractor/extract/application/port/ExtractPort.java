package com.extractor.extract.application.port;

import com.extractor.extract.domain.model.FileDocument;
import com.extractor.extract.domain.model.ExtractHwpxDocument;
import com.extractor.extract.domain.model.ExtractPdfDocument;

public interface ExtractPort {

    /**
     * 한글 문서 추출
     *
     * @param fileDocument 원본 문서 정보
     */
    ExtractHwpxDocument extractHwpxDocumentPort(FileDocument fileDocument);

    /**
     * PDF 문서 추출
     *
     * @param fileDocument 원본 문서 정보
     */
    ExtractPdfDocument extractPdfDocumentPort(FileDocument fileDocument);

    /**
     * 문서 텍스트 추출
     *
     * @param fileDocument 원본 문서 정보
     */
    String extractDocumentPort(FileDocument fileDocument);
}
