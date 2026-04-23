package com.genai.extractor.adapter.out.repository;

import com.genai.extractor.adapter.out.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
