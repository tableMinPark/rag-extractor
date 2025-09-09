package com.extractor.application.port;

import com.extractor.domain.model.law.LawDocument;

public interface LawPersistencePort {

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    LawDocument getLawDocumentsPort(Long lawId);
}