package com.extractor.application.port;

import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.Source;

public interface SourcePersistencePort {

    /**
     * 원본 문서 저장
     *
     * @param source 원본 문서
     * @return 원본 문서
     */
    Source createSourcePort(Source source);

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
