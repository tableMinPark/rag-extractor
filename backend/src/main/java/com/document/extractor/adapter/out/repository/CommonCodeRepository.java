package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.CommonCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCodeEntity, Long> {

    Optional<CommonCodeEntity> findByCode(String code);

    List<CommonCodeEntity> findByCodeGroup(String codeGroup);
}
