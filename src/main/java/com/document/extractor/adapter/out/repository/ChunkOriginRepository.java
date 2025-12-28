package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.ChunkOriginEntityForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkOriginRepository extends JpaRepository<ChunkOriginEntityForTest, Long> {
}

