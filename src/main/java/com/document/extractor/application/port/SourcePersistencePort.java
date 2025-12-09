package com.document.extractor.application.port;

import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.model.Source;

import java.util.List;

public interface SourcePersistencePort {

    /**
     * 대상 문서 등록
     *
     * @param source 대상 문서
     * @return 대상 문서
     */
    Source saveSourcePort(Source source);

    /**
     * 대상 문서 조회
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    Source getSourcePort(Long sourceId);

    /**
     * 대상 문서 조회 (비관락)
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    Source getSourceWithLockPort(Long sourceId);

    /**
     * 대상 문서 목록 조회
     *
     * @param page    페이지
     * @param size    사이즈
     * @param orderBy 정렬 필드
     * @param order   정렬 방향 ( asc | desc )
     * @param keyword 키워드
     * @param isAuto  자동화 여부
     * @return 대상 문서 목록
     */
    PageWrapper<Source> getSourcesPort(int page, int size, String orderBy, String order, String keyword, boolean isAuto);

    /**
     * 배치 대상 문서 목록 조회
     *
     * @return 대상 문서 목록
     */
    List<Source> getActiveSourcesPort();

}