package com.extractor.global.adapter.out.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_law")
public class LawDocumentEntity {

    @Id
    @Column(name = "seq", nullable = false)
    private Long lawId;

    @Column(name = "lawname")
    private String lawName;

    @Column(name = "lawgroup")
    private Integer lawGroup;

    @OneToMany
    @JoinColumn(name = "seq")
    private List<LawHistoryEntity> versions;

    /**
     * 가장 최근 버전 조회
     *
     * @return 가장 최근 버전
     */
    public Integer getLatestVersion() {
        versions.sort((v1, v2) -> v2.getVersion() - v1.getVersion());
        return versions.getFirst() != null ? versions.getFirst().getVersion() : -1;
    }
}
