package com.document.extractor.application.port;

import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Passage;
import com.document.extractor.domain.model.Source;

import java.util.List;
import java.util.Optional;

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
    Optional<Source> getSourcePort(Long sourceId);

    /**
     * 대상 문서 조회 (비관락)
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    Optional<Source> getSourcePortWithLock(Long sourceId);

    /**
     * 배치 대상 문서 조회
     *
     * @return 대상 문서 목록
     */
    List<Source> getActiveSourcesPort();

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