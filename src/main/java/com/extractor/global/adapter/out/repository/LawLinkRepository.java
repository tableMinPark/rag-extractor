package com.extractor.global.adapter.out.repository;

import com.extractor.global.adapter.out.entity.LawLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LawLinkRepository extends JpaRepository<LawLinkEntity, Long> {

    /**
     * 법령 본문 ID, 버전 기준 법령 연결 목록 조회
     *
     * @param lawContentId 법령 본문 ID
     * @param version      버전
     */
    @Query(value =
        "select distinct on (seq, seq_contents, text_al, link_al, seq_history) * " +
        "  from nhis.tbl_law_autolink " +
        " where seq_contents = :lawContentId " +
        "   and seq_history = :version " +
        " order by seq_history ", nativeQuery = true)
    List<LawLinkEntity> findDistinctByLawContentIdAndVersion(Long lawContentId, Integer version);
}