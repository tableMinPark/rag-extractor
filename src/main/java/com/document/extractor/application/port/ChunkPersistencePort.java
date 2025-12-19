package com.document.extractor.application.port;

import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.model.Chunk;

import java.util.List;

public interface ChunkPersistencePort {

    /**
     * 청크 조회
     *
     * @param chunkId 청크 ID
     * @return 청크
     */
    Chunk getChunkPort(Long chunkId);

    /**
     * 정렬 필드, 버전 코드 기준 청크 목록 조회
     *
     * @param sortOrder 정렬 필드
     * @param version   버전 코드
     * @return 청크 목록
     */
    List<Chunk> getChunkBySortOrderAndVersionPort(Long sourceId, Integer sortOrder, Long version);

    /**
     * 청크 목록 조회
     *
     * @param page 페이지
     * @param size 사이즈
     * @param passageId 패시지 ID
     * @return 청크 목록
     */
    PageWrapper<Chunk> getChunksPort(int page, int size, long passageId);

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

    /**
     * 청크 삭제
     *
     * @param chunkId 청크 ID
     */
    void deleteChunkPort(Long chunkId);
}
