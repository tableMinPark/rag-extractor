package com.extractor.application.port;

import com.extractor.domain.model.Document;

public interface LawReadPort {

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    Document getLawDocumentsPort(Long lawId);
}