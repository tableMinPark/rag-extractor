package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChunkRepository extends JpaRepository<ChunkEntity, Long> {

    List<ChunkEntity> findByPassageIdAndVersion(Long passageId, Long version);

    Page<ChunkEntity> findByPassageId(Long passageId, Pageable pageable);
}
