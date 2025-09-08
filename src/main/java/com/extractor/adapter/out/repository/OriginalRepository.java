package com.extractor.adapter.out.repository;

import com.extractor.adapter.out.entity.OriginalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginalRepository extends JpaRepository<OriginalDocumentEntity, Long> {
}
