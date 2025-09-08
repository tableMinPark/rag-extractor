package com.extractor.adapter.out.repository;

import com.extractor.adapter.out.entity.LawDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LawDocumentRepository extends JpaRepository<LawDocumentEntity, Long> {

    /**
     * 법령 ID 기준 법령 조회
     * @param lawId 법령 ID
     */
    Optional<LawDocumentEntity> findByLawId(Long lawId);
}
