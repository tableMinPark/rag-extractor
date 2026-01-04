package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.ChunkTmpEntityForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChunkTmpRepository extends JpaRepository<ChunkTmpEntityForTest, Long> {

    List<ChunkTmpEntityForTest> findAllByChunkIdOrderById(Long chunkId);

}

