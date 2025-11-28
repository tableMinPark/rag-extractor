package com.extractor.adapter.out.repository;

import com.extractor.adapter.out.entity.ChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<ChunkEntity, Long> {
}
