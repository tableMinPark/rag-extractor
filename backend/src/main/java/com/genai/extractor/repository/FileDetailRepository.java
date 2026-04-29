package com.genai.extractor.repository;

import com.genai.extractor.repository.entity.FileDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDetailRepository extends JpaRepository<FileDetailEntity, Long> {
}
