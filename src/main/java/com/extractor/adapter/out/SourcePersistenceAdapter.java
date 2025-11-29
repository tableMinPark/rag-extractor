package com.extractor.adapter.out;

import com.extractor.adapter.out.repository.ChunkRepository;
import com.extractor.adapter.out.repository.PassageRepository;
import com.extractor.adapter.out.repository.SourceRepository;
import com.extractor.application.port.SourcePersistencePort;
import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.Source;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.SourcePattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourcePersistenceAdapter implements SourcePersistencePort {

    private final SourceRepository sourceRepository;
    private final PassageRepository passageRepository;
    private final ChunkRepository chunkRepository;

    /**
     * 대상 문서 등록
     *
     * @param source 대상 문서
     * @return 대상 문서
     */
    @Override
    public Source createSourcePort(Source source) {
        return null;
    }

    /**
     * 대상 문서 패턴 등록
     *
     * @param sourcePattern 대상 문서 패턴
     * @return 대상 문서 패턴
     */
    @Override
    public SourcePattern createSourcePatternPort(SourcePattern sourcePattern) {
        return null;
    }

    /**
     * 패시지 저장
     *
     * @param passage 패시지
     * @return 패시지
     */
    @Override
    public Passage createPassagePort(Passage passage) {
        return null;
    }

    /**
     * 청크 저장
     *
     * @param chunk 청크
     * @return 청크
     */
    @Override
    public Chunk createChunkPort(Chunk chunk) {
        return null;
    }
}