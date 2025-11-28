package com.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_SOURCE_ID_SEQ",
        sequenceName = "WN_SOURCE_SOURCE_ID_SEQ",
        allocationSize = 1
)
public class SourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_SOURCE_ID_SEQ")
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "version")
    private String version;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "name")
    private String name;

    @Column(name = "content")
    private String content;

    @Column(name = "collection_id")
    private String collectionId;

    @Column(name = "file_detail_id")
    private Long fileDetailId;

    @Column(name = "sys_create_dt")
    private LocalDateTime sysCreateDt;

    @Column(name = "sys_modify_dt")
    private LocalDateTime sysModifyDt;
}
