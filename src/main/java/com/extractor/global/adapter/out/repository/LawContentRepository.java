package com.extractor.global.adapter.out.repository;

import com.extractor.global.adapter.out.entity.LawContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LawContentRepository extends JpaRepository<LawContentEntity, Long> {

    /**
     * 법령 ID 기준 가장 최근 버전의 법령 본문 엔티티 목록 조회
     *
     * @param lawId    법령 ID
     * @param version  버전
     */
    List<LawContentEntity> findByLawIdAndVersionOrderByArrange(Long lawId, Integer version);

    /**
     * 링크 코드 목록 기준 법령 본문 엔티티 목록 조회
     *
     * @param lawId    법령 ID
     * @param linkCode 링크 코드
     */
    List<LawContentEntity> findTopByLawIdAndLinkCodeOrderByVersionDesc(Long lawId, String linkCode);
}
