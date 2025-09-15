package com.extractor.chunk.application.port;

import com.extractor.chunk.domain.model.LawDocument;

public interface LawPersistencePort {

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    LawDocument getLawDocumentsPort(Long lawId);
}