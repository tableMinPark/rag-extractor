package com.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "wn_original")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "originalIdGenerator",
        sequenceName = "wn_original_original_id_seq",
        allocationSize = 1
)
public class OriginalDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "originalIdGenerator")
    @Column(name = "original_id", nullable = false)
    private Long originalId;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "name")
    private String name;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public OriginalDocumentEntity(String docType, String categoryCode, String name, String filePath, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.docType = docType;
        this.categoryCode = categoryCode;
        this.name = name;
        this.filePath = filePath;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}