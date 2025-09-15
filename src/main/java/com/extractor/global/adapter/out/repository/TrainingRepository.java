package com.extractor.global.adapter.out.repository;

import com.extractor.global.adapter.out.entity.TrainingDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<TrainingDocumentEntity, Long> {
}
