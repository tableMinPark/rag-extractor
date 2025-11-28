package com.extractor.application.port;

import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDocument;
import com.extractor.global.enums.ExtractType;

public interface ExtractPort {

    /**
     * 한글 문서 추출
     *
     * @param fileDocument 원본 문서 정보
     */
    Document extractHwpxPort(FileDocument fileDocument, ExtractType extractType);

    /**
     * PDF 문서 추출
     *
     * @param fileDocument 원본 문서 정보
     */
    Document extractPdfPort(FileDocument fileDocument);

    /**
     * 문서 텍스트 추출
     *
     * @param fileDocument 원본 문서 정보
     */
    String extractTextPort(FileDocument fileDocument);
}
