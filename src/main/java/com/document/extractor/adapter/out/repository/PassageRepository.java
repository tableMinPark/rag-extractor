package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.PassageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassageRepository extends JpaRepository<PassageEntity, Long> {
}
