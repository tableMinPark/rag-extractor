package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<ChunkEntity, Long> {
}
