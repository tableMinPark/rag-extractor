package com.extractor.application.port;

import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.Source;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.SourcePattern;

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
     * @param sourcePattern 대상 문서 패턴
     * @return 대상 문서 패턴
     */
    SourcePattern createSourcePatternPort(SourcePattern sourcePattern);

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
