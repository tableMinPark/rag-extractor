package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import com.document.extractor.adapter.out.entity.PassageEntity;
import com.document.extractor.adapter.out.entity.SourceEntity;
import com.document.extractor.adapter.out.repository.ChunkRepository;
import com.document.extractor.adapter.out.repository.PassageRepository;
import com.document.extractor.adapter.out.repository.SourceRepository;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.PassagePersistencePort;
import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Passage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassagePersistenceAdapter implements PassagePersistencePort {

    private final SourceRepository sourceRepository;
    private final PassageRepository passageRepository;
    private final ChunkRepository chunkRepository;

    /**
     * 최신 패시지 목록 조회
     *
     * @param sourceId 대상 문서 ID
     * @return 패시지 목록
     */
    @Transactional
    @Override
    public PageWrapper<Passage> getLatestPassagesPort(int page, int size, long sourceId) {

        SourceEntity sourceEntity = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"));

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<PassageEntity> passageEntities = passageRepository.findBySourceIdAndVersionOrderBySortOrderAsc(sourceId, sourceEntity.getVersion(), pageable);

        return PageWrapper.<Passage>builder()
                .data(passageEntities.stream().map(PassageEntity::toDomain).toList())
                .isLast(passageEntities.isLast())
                .page(passageEntities.getNumber() + 1)
                .size(passageEntities.getSize())
                .totalCount(passageEntities.getTotalElements())
                .totalPages(passageEntities.getTotalPages())
                .build();
    }

    /**
     * 패시지 목록 조회
     *
     * @param sourceId 대상 문서 ID
     * @param version  버전
     * @return 패시지 목록
     */
    @Transactional
    @Override
    public List<Passage> getPassagesByVersionPort(Long sourceId, long version) {
        return passageRepository.findBySourceIdAndVersionOrderBySortOrderAsc(sourceId, version).stream()
                .map(PassageEntity::toDomain)
                .toList();
    }

    /**
     * 패시지 조회
     *
     * @param passageId 패시지 Id
     * @return 패시지
     */
    @Transactional
    @Override
    public Passage getPassagePort(Long passageId) {
        return passageRepository.findById(passageId)
                .orElseThrow(() -> new NotFoundException("패시지"))
                .toDomain();
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
            passageEntity = passageRepository.findById(passage.getPassageId()).orElseThrow(() -> new NotFoundException("패시지"));
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
                        : passageRepository.findById(passage.getPassageId()).orElseThrow(() -> new NotFoundException("패시지")).update(passage))
                .toList();

        return passageRepository.saveAll(passageEntities).stream().map(PassageEntity::toDomain).toList();
    }

    /**
     * 정렬 필드, 버전 코드 기준 청크 목록 조회
     *
     * @param sortOrder 정렬 필드
     * @param version   버전 코드
     * @return 청크 목록
     */
    @Transactional
    @Override
    public List<Chunk> getChunkBySortOrderAndVersionPort(Long sourceId, Integer sortOrder, Long version) {

        PassageEntity passageEntity = passageRepository.findBySourceIdAndSortOrderAndVersion(sourceId, sortOrder, version)
                .orElseThrow(() -> new NotFoundException("패시지"));

        return chunkRepository.findByPassageId(passageEntity.getPassageId()).stream()
                .map(ChunkEntity::toDomain)
                .toList();
    }

    /**
     * 청크 저장
     *
     * @param chunk 청크
     * @return 청크
     */
    @Transactional
    @Override
    public Chunk saveChunkPortPort(Chunk chunk) {

        ChunkEntity chunkEntity;

        if (chunk.getChunkId() == null) {
            chunkEntity = chunkRepository.save(ChunkEntity.fromDomain(chunk));
        } else {
            chunkEntity = chunkRepository.findById(chunk.getChunkId()).orElseThrow(() -> new NotFoundException("청크"));
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
                        : chunkRepository.findById(chunk.getChunkId()).orElseThrow(() -> new NotFoundException("청크")).update(chunk))
                .toList();

        return chunkRepository.saveAll(chunkEntities).stream().map(ChunkEntity::toDomain).toList();
    }
}
