package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.PassageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassageRepository extends JpaRepository<PassageEntity, Long> {

    List<PassageEntity> findBySourceIdAndVersionOrderBySortOrderAsc(Long sourceId, Long version);

    Optional<PassageEntity> findBySourceIdAndSortOrderAndVersion(Long sourceId, Integer sortOrder, Long version);
}
