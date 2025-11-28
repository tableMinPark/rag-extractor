package com.extractor.adapter.out;

import com.extractor.adapter.out.repository.ChunkRepository;
import com.extractor.adapter.out.repository.PassageRepository;
import com.extractor.adapter.out.repository.SourceRepository;
import com.extractor.application.port.SourcePersistencePort;
import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourcePersistenceAdapter implements SourcePersistencePort {

    private final SourceRepository sourceRepository;

    private final PassageRepository passageRepository;

    private final ChunkRepository chunkRepository;

    @Override
    public Source createSourcePort(Source source) {
        return null;
    }

    @Override
    public Passage createPassagePort(Passage passage) {
        return null;
    }

    @Override
    public Chunk createChunkPort(Chunk chunk) {
        return null;
    }
}