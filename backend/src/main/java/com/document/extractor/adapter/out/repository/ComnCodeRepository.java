package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.ComnCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComnCodeRepository extends JpaRepository<ComnCodeEntity, Long> {

    Optional<ComnCodeEntity> findByCode(String code);

    List<ComnCodeEntity> findByCodeGroup(String codeGroup);
}
