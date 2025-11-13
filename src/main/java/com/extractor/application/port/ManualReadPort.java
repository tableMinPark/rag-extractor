package com.extractor.application.port;

import com.extractor.domain.model.Document;

public interface ManualReadPort {

    /**
     * 메뉴얼 문서 조회
     *
     * @param manualId 메뉴얼 ID
     */
    Document getManualDocumentsPort(Long manualId);
}
