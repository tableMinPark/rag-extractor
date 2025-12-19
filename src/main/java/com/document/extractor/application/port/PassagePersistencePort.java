package com.document.extractor.application.port;

import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Passage;

import java.util.List;

public interface PassagePersistencePort {

    /**
     * 최신 패시지 목록 조회
     *
     * @param page 페이지
     * @param size 사이즈
     * @param sourceId 대상 문서 ID
     * @return 패시지 목록
     */
    PageWrapper<Passage> getLatestPassagesPort(int page, int size, long sourceId);

    /**
     * 패시지 목록 조회
     *
     * @param sourceId 대상 문서 ID
     * @param version  버전
     * @return 패시지 목록
     */
    List<Passage> getPassagesByVersionPort(Long sourceId, long version);

    /**
     * 패시지 조회
     *
     * @param passageId 패시지 Id
     * @return 패시지
     */
    Passage getPassagePort(Long passageId);

    /**
     * 패시지 저장
     *
     * @param passage 패시지
     * @return 패시지
     */
    Passage savePassagePort(Passage passage);

    /**
     * 패시지 목록 저장
     *
     * @param passages 패시지 목록
     * @return 패시지 목록
     */
    List<Passage> savePassagesPort(List<Passage> passages);
}
