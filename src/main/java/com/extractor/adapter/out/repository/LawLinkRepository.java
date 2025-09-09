package com.extractor.adapter.out.repository;

import com.extractor.adapter.out.entity.LawLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LawLinkRepository extends JpaRepository<LawLinkEntity, Long> {

    /**
     * 법령 본문 ID, 버전 기준 법령 연결 목록 조회
     *
     * @param lawContentId 법령 본문 ID
     * @param version      버전
     */
    List<LawLinkEntity> findByLawContentIdAndVersion(Long lawContentId, Integer version);
}