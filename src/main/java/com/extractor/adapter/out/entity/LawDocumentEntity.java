package com.extractor.adapter.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
