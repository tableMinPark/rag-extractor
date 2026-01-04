package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.FileDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDetailRepository extends JpaRepository<FileDetailEntity, Long> {
}
