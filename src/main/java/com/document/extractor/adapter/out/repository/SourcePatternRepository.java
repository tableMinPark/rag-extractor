package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.SourcePatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourcePatternRepository extends JpaRepository<SourcePatternEntity, Long> {
}
