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
@Table(name = "tbl_manual")
public class ManualDocumentEntity {

    @Id
    @Column(name = "itemid")
    private Long itemId;

    private String title;

    private String tableContent;

    private String content;

    private String fileContent;
}
