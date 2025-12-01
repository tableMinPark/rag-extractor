package com.document.extractor.application.port;

import com.document.extractor.domain.model.*;

import java.util.List;

public interface SourcePersistencePort {

    /**
     * 대상 문서 등록
     *
     * @param source 대상 문서
     * @return 대상 문서
     */
    Source createSourcePort(Source source);

    /**
     * 대상 문서 패턴 등록
     *
     * @param sourcePatterns     대상 문서 패턴
     * @param sourceStopPatterns 대상 문서 중단 패턴
     */
    void createSourcePatternPort(List<SourcePattern> sourcePatterns, List<SourceStopPattern> sourceStopPatterns);

    /**
     * 패시지 저장
     *
     * @param passage 패시지
     * @return 패시지
     */
    Passage createPassagePort(Passage passage);

    /**
     * 청크 저장
     *
     * @param chunk 청크
     * @return 청크
     */
    Chunk createChunkPort(Chunk chunk);
}
