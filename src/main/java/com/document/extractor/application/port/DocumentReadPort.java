package com.document.extractor.application.port;

import com.document.extractor.domain.model.Document;

public interface DocumentReadPort {

    /**
     * 원격 문서 조회
     *
     * @param uri             원격 문서 URI
     * @param extractTypeCode 표 추출 타입 코드
     */
    Document getRepoDocumentPort(String uri, String extractTypeCode);
}
