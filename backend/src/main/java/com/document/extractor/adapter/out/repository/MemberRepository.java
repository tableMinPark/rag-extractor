package com.document.extractor.adapter.out.repository;

import com.document.extractor.adapter.out.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByName(String name);
}
