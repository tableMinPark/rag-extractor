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
@Table(name = "tbl_law_content")
public class LawContentEntity {

    @Id
    @Column(name = "seq_contents", nullable = false)
    private Long lawContentId;

    @Column(name = "seq")
    private Long lawId;

    @Column(name = "seq_history")
    private Integer version;

    @Column(name = "type")
    private String contentType;

    @Column(name = "gubun")
    private String categoryCode;

    @Column(name = "arrange")
    private Integer arrange;

    @Column(name = "linkcode")
    private String linkCode;

    @Column(name = "articleno")
    private String simpleTitle;

    @Column(name = "title")
    private String title;

    @Column(name = "contents")
    private String content;
}
