package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.SourceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<SourceEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SourceEntity> findBySourceId(Long sourceId);

    List<SourceEntity> findByIsAutoTrueOrderBySourceId();

    Page<SourceEntity> findAllByIsAuto(boolean isAuto, Pageable pageable);

    Page<SourceEntity> findAllByIsAutoAndNameLike(boolean isAuto, String keyword, Pageable pageable);
}
