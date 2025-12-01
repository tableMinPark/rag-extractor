package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.Chunk;
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
    @Column(name = "chunk_id", nullable = false, updatable = false)
    private Long chunkId;

    @Column(name = "passage_id")
    private Long passageId;

    @Column(name = "version")
    private Long version;

    @Column(name = "title")
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Column(name = "third_title")
    private String thirdTitle;

    @Column(name = "content")
    private String content;

    @Column(name = "compact_content")
    private String compactContent;

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

    public void update(Chunk chunk) {
        this.passageId = chunk.getPassageId();
        this.version = chunk.getVersion();
        this.title = chunk.getTitle();
        this.subTitle = chunk.getSubTitle();
        this.thirdTitle = chunk.getThirdTitle();
        this.content = chunk.getContent();
        this.subContent = chunk.getSubContent();
        this.compactContent = chunk.getCompactContent();
        this.tokenSize = chunk.getTokenSize();
    }

    public Chunk toDomain() {
        return Chunk.builder()
                .chunkId(chunkId)
                .passageId(passageId)
                .version(version)
                .title(title)
                .subTitle(subTitle)
                .thirdTitle(thirdTitle)
                .content(content)
                .subContent(subContent)
                .compactContent(compactContent)
                .tokenSize(tokenSize)
                .sysCreateDt(sysCreateDt)
                .sysModifyDt(sysModifyDt)
                .build();
    }

    public static ChunkEntity fromDomain(Chunk chunk) {
        return ChunkEntity.builder()
                .chunkId(chunk.getChunkId())
                .passageId(chunk.getPassageId())
                .version(chunk.getVersion())
                .title(chunk.getTitle())
                .subTitle(chunk.getSubTitle())
                .thirdTitle(chunk.getThirdTitle())
                .content(chunk.getContent())
                .subContent(chunk.getSubContent())
                .compactContent(chunk.getCompactContent())
                .tokenSize(chunk.getTokenSize())
                .build();
    }
}
