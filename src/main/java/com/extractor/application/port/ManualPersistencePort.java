package com.extractor.application.port;

import com.extractor.domain.model.Document;

public interface ManualPersistencePort {

    /**
     * 메뉴얼 문서 조회
     *
     * @param manualId 메뉴얼 ID
     */
    Document getManualDocumentsPort(Long manualId);
}
