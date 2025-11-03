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
@Table(name = "wn_training")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "trainingIdGenerator",
        sequenceName = "wn_training_training_id_seq",
        allocationSize = 1
)
public class TrainingDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainingIdGenerator")
    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "original_id")
    private Long originalId;

    @Column(name = "version")
    private String version;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "title")
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Column(name = "third_title")
    private String thirdTitle;

    @Column(name = "content")
    private String content;

    @Column(name = "sub_content")
    private String subContent;

    @Column(name = "token_size")
    private Integer tokenSize;

    @Column(name = "sys_create_dt")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "sys_modify_dt")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public TrainingDocumentEntity(Long originalId, String version, String docType, String categoryCode, String title, String subTitle, String thirdTitle, String content, String subContent, Integer tokenSize) {
        this.originalId = originalId;
        this.version = version;
        this.docType = docType;
        this.categoryCode = categoryCode;
        this.title = title;
        this.subTitle = subTitle;
        this.thirdTitle = thirdTitle;
        this.content = content;
        this.subContent = subContent;
        this.tokenSize = tokenSize;
    }
}
