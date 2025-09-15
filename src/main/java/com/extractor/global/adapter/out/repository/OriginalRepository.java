package com.extractor.global.adapter.out.repository;

import com.extractor.global.adapter.out.entity.OriginalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginalRepository extends JpaRepository<OriginalDocumentEntity, Long> {
}
