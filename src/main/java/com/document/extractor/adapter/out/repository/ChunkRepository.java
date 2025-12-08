package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChunkRepository extends JpaRepository<ChunkEntity, Long> {

    List<ChunkEntity> findByPassageId(Long passageId);
}
