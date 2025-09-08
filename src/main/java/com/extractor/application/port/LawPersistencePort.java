package com.extractor.application.port;

import com.extractor.domain.model.law.LawDocument;
import com.extractor.domain.model.law.LawLink;

import java.util.List;

public interface LawPersistencePort {

    /**
     * 법령 문서 조회
     * @param lawId 법령 ID
     */
    LawDocument getLawDocumentsPort(Long lawId);

    /**
     * 법령 본문 ID 기준 법령 연결 정보 목록 조회
     * @param lawContentId 법령 본문 ID
     */
    List<LawLink> getLawLinksPort(Long lawContentId, Integer version);
}
