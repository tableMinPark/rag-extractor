package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import com.document.extractor.adapter.out.entity.PassageEntity;
import com.document.extractor.adapter.out.repository.ChunkRepository;
import com.document.extractor.adapter.out.repository.PassageRepository;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.ChunkPersistencePort;
import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.model.Chunk;
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
public class ChunkPersistenceAdapter implements ChunkPersistencePort {

    private final PassageRepository passageRepository;
    private final ChunkRepository chunkRepository;

    /**
     * 청크 조회
     *
     * @param chunkId 청크 ID
     * @return 청크
     */
    @Transactional(readOnly = true)
    @Override
    public Chunk getChunkPort(Long chunkId) {
        return chunkRepository.findById(chunkId)
                .orElseThrow(() -> new NotFoundException("청크"))
                .toDomain();
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

        return chunkRepository.findByPassageIdAndVersion(passageEntity.getPassageId(), passageEntity.getVersion()).stream()
                .map(ChunkEntity::toDomain)
                .toList();
    }

    /**
     * 청크 목록 조회
     *
     * @param page 페이지
     * @param size 사이즈
     * @param passageId 패시지 ID
     * @return 청크 목록
     */
    @Transactional
    @Override
    public PageWrapper<Chunk> getChunksPort(int page, int size, long passageId) {

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<ChunkEntity> chunkEntities = chunkRepository.findByPassageId(passageId, pageable);

        return PageWrapper.<Chunk>builder()
                .data(chunkEntities.stream().map(ChunkEntity::toDomain).toList())
                .isLast(chunkEntities.isLast())
                .page(chunkEntities.getNumber() + 1)
                .size(chunkEntities.getSize())
                .totalCount(chunkEntities.getTotalElements())
                .totalPages(chunkEntities.getTotalPages())
                .build();
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

    /**
     * 청크 삭제
     *
     * @param chunkId 청크 ID
     */
    @Transactional
    @Override
    public void deleteChunkPort(Long chunkId) {
        chunkRepository.deleteById(chunkId);
    }
}
