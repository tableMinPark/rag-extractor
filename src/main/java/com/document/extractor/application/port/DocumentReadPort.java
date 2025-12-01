package com.document.extractor.application.port;

import com.document.extractor.domain.model.Document;
import com.document.global.enums.ExtractType;

public interface DocumentReadPort {

    /**
     * 원격 문서 조회
     *
     * @param repoType    원격 문서 타입
     * @param repoId      원격 문서 ID
     * @param extractType 표 추출 타입
     */
    Document getRepoDocumentPort(String repoType, String repoId, ExtractType extractType);

    /**
     * 원격 문서 조회
     *
     * @param url    원격 문서 URL
     * @param extractType 표 추출 타입
     */
    Document getRepoDocumentPort(String url, ExtractType extractType);
}
