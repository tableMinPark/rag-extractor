package com.document.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_CHUNK")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_CHUNK_CHUNK_ID_SEQ",
        sequenceName = "WN_CHUNK_CHUNK_ID_SEQ",
        allocationSize = 1
)
public class ChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_CHUNK_CHUNK_ID_SEQ")
    @Column(name = "chunk_id", nullable = false)
    private Long chunkId;

    @Column(name = "passage_id")
    private Long passageId;

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

    @CreatedDate
    @Column(name = "sys_create_dt")
    private LocalDateTime sysCreateDt;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    private LocalDateTime sysModifyDt;
}
