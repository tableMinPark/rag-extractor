package com.document.extractor.application.port;

import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Passage;

import java.util.List;

public interface PassagePersistencePort {

    /**
     * 패시지 목록 조회
     *
     * @param sourceId 대상 문서 ID
     * @param version  버전
     * @return 패시지 목록
     */
    List<Passage> getPassagesByVersion(long sourceId, long version);

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

    /**
     * 정렬 필드, 버전 코드 기준 청크 목록 조회
     *
     * @param sortOrder 정렬 필드
     * @param version   버전 코드
     * @return 청크 목록
     */
    List<Chunk> getChunkBySortOrderAndVersion(int sortOrder, long version);

    /**
     * 청크 저장
     *
     * @param chunk 청크
     * @return 청크
     */
    Chunk saveChunkPort(Chunk chunk);

    /**
     * 청크 목록 저장
     *
     * @param chunks 청크 목록
     * @return 청크 목록
     */
    List<Chunk> saveChunksPort(List<Chunk> chunks);
}
