package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.PassageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassageRepository extends JpaRepository<PassageEntity, Long> {

    Page<PassageEntity> findBySourceIdAndVersionOrderBySortOrderAsc(Long sourceId, Long version, Pageable pageable);

    List<PassageEntity> findBySourceIdAndVersionOrderBySortOrderAsc(Long sourceId, Long version);

    Optional<PassageEntity> findBySourceIdAndSortOrderAndVersion(Long sourceId, Integer sortOrder, Long version);
}
