package com.extractor.adapter.out.repository;

import com.extractor.adapter.out.entity.ManualDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualDocumentRepository extends JpaRepository<ManualDocumentEntity, Long> {
}