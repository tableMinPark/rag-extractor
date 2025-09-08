package com.extractor.adapter.out.repository;

import com.extractor.adapter.out.entity.LawContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LawContentRepository extends JpaRepository<LawContentEntity, Long> {

    /**
     * 법령 ID 기준 가장 최근 버전의 법령 본문 엔티티 목록 조회
     * @param lawId 법령 ID
     */
    @Query( "SELECT lc " +
            "  FROM LawContentEntity lc " +
            " WHERE lc.version = ( " +
            "SELECT lc2.version " +
            "  FROM LawContentEntity lc2 " +
            " WHERE lc2.lawId = :lawId " +
            " ORDER BY lc2.version DESC " +
            " LIMIT 1 " +
            " ) " +
            " ORDER BY lc.arrange ")
    List<LawContentEntity> findByLawId(Long lawId);
}
