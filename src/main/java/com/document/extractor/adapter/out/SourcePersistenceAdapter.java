package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import com.document.extractor.adapter.out.entity.PassageEntity;
import com.document.extractor.adapter.out.entity.SourceEntity;
import com.document.extractor.adapter.out.repository.ChunkRepository;
import com.document.extractor.adapter.out.repository.PassageRepository;
import com.document.extractor.adapter.out.repository.SourceRepository;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.SourcePersistencePort;
import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Passage;
import com.document.extractor.domain.model.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Transactional
    @Override
    public Source saveSourcePort(Source source) {

        SourceEntity sourceEntity;

        if (source.getSourceId() == null) {
            sourceEntity = sourceRepository.save(SourceEntity.fromDomain(source));
        } else {
            sourceEntity = sourceRepository.findById(source.getSourceId()).orElseThrow(NotFoundException::new);
            sourceEntity.update(source);
            sourceEntity = sourceRepository.save(sourceEntity);
        }

        return sourceEntity.toDomain();
    }

    /**
     * 대상 문서 조회
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Optional<Source> getSourcePort(Long sourceId) {
        return sourceRepository.findById(sourceId).map(SourceEntity::toDomain);
    }

    /**
     * 대상 문서 조회 (비관락)
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Optional<Source> getSourcePortWithLock(Long sourceId) {
        return sourceRepository.findBySourceId(sourceId).map(SourceEntity::toDomain);
    }

    /**
     * 배치 대상 문서 조회
     *
     * @return 대상 문서 목록
     */
    @Transactional
    @Override
    public List<Source> getActiveSourcesPort() {
        return sourceRepository.findByIsActiveTrueOrderBySourceId().stream()
                .map(SourceEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 패시지 저장
     *
     * @param passage 패시지
     * @return 패시지
     */
    @Transactional
    @Override
    public Passage savePassagePort(Passage passage) {

        PassageEntity passageEntity;

        if (passage.getPassageId() == null) {
            passageEntity = passageRepository.save(PassageEntity.fromDomain(passage));
        } else {
            passageEntity = passageRepository.findById(passage.getPassageId()).orElseThrow(NotFoundException::new);
            passageEntity.update(passage);
            passageEntity = passageRepository.save(passageEntity);
        }

        return passageEntity.toDomain();
    }

    /**
     * 패시지 목록 저장
     *
     * @param passages 패시지 목록
     * @return 패시지 목록
     */
    @Transactional
    @Override
    public List<Passage> savePassagesPort(List<Passage> passages) {

        List<PassageEntity> passageEntities = passages.stream()
                        .map(passage -> passage.getPassageId() == null
                                ? PassageEntity.fromDomain(passage)
                                : passageRepository.findById(passage.getPassageId()).orElseThrow(NotFoundException::new))
                        .toList();

        return passageRepository.saveAll(passageEntities).stream().map(PassageEntity::toDomain).toList();
    }

    /**
     * 청크 저장
     *
     * @param chunk 청크
     * @return 청크
     */
    @Transactional
    @Override
    public Chunk saveChunkPort(Chunk chunk) {

        ChunkEntity chunkEntity;

        if (chunk.getChunkId() == null) {
            chunkEntity = chunkRepository.save(ChunkEntity.fromDomain(chunk));
        } else {
            chunkEntity = chunkRepository.findById(chunk.getChunkId()).orElseThrow(NotFoundException::new);
            chunkEntity.update(chunk);
            chunkEntity = chunkRepository.save(chunkEntity);
        }

        return chunkEntity.toDomain();
    }

    /**
     * 청크 목록 저장
     *
     * @param chunks 청크 목록
     * @return 청크 목록
     */
    @Transactional
    @Override
    public List<Chunk> saveChunksPort(List<Chunk> chunks) {

        List<ChunkEntity> chunkEntities = chunks.stream()
                .map(chunk -> chunk.getChunkId() == null
                        ? ChunkEntity.fromDomain(chunk)
                        : chunkRepository.findById(chunk.getChunkId()).orElseThrow(NotFoundException::new))
                .toList();

        return chunkRepository.saveAll(chunkEntities).stream().map(ChunkEntity::toDomain).toList();
    }
}