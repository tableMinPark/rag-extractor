package com.extractor.chunk.application.port;

import com.extractor.extract.domain.model.ExtractHwpxDocument;
import com.extractor.extract.domain.model.ExtractPdfDocument;
import com.extractor.extract.domain.model.FileDocument;

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
}
