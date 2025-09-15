package com.extractor.global.adapter.out.entity;

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
@Table(name = "tbl_law_autolink")
public class LawLinkEntity {

    @Id
    @Column(name = "seq_al", nullable = false)
    private Long lawLinkId;

    @Column(name = "seq_contents")
    private Long lawContentId;

    @Column(name = "seq")
    private Long lawId;

    @Column(name = "seq_history")
    private Integer version;

    @Column(name = "text_al")
    private String content;

    @Column(name = "link_al")
    private String linkTag;
}
