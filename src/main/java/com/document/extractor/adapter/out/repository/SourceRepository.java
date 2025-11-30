package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.SourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<SourceEntity, Long> {
}
