package com.document.extractor.application.port;

import com.document.extractor.domain.model.Document;

public interface DocumentReadPort {

    /**
     * 원격 문서 조회
     *
     * @param repoType        원격 문서 타입
     * @param repoId          원격 문서 ID
     * @param extractTypeCode 표 추출 타입 코드
     */
    Document getRepoDocumentPort(String repoType, String repoId, String extractTypeCode);

    /**
     * 원격 문서 조회
     *
     * @param repoType        원격 문서 타입
     * @param uri             원격 문서 URI
     * @param extractTypeCode 표 추출 타입 코드
     */
    Document getRepoDocumentByUriPort(String repoType, String uri, String extractTypeCode);
}
