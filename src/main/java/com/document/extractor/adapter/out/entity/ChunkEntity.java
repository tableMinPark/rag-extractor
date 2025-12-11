package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.Chunk;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
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
@Comment("청크")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_CHUNK_ID_SEQ",
        sequenceName = "WN_CHUNK_ID_SEQ",
        allocationSize = 1
)
public class ChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_CHUNK_ID_SEQ")
    @Column(name = "chunk_id", nullable = false, updatable = false)
    @Comment("청크 ID")
    private Long chunkId;

    @Column(name = "passage_id")
    @Comment("패시지 ID")
    private Long passageId;

    @Column(name = "version")
    @Comment("버전 코드")
    private Long version;

    @Column(name = "title", length = 4000)
    @Comment("제목")
    private String title;

    @Column(name = "sub_title", length = 4000)
    @Comment("중제목")
    private String subTitle;

    @Column(name = "third_title", length = 4000)
    @Comment("소제목")
    private String thirdTitle;

//    @Lob
    @Column(name = "content")
    @Comment("본문")
    private String content;

//    @Lob
    @Column(name = "compact_content")
    @Comment("색인 대상 본문")
    private String compactContent;

//    @Lob
    @Column(name = "sub_content")
    @Comment("부가 본문")
    private String subContent;

    @Column(name = "token_size")
    @Comment("본문 토큰 크기")
    private Integer tokenSize;

    @Column(name = "compact_token_size")
    @Comment("색인 대상 본문 토큰 크기")
    private Integer compactTokenSize;

    @CreatedDate
    @Column(name = "sys_create_dt")
    @Comment("생성 일자")
    private LocalDateTime sysCreateDt;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    @Comment("수정 일자")
    private LocalDateTime sysModifyDt;

    public ChunkEntity update(Chunk chunk) {
        this.passageId = chunk.getPassageId();
        this.version = chunk.getVersion();
        this.title = chunk.getTitle();
        this.subTitle = chunk.getSubTitle();
        this.thirdTitle = chunk.getThirdTitle();
        this.content = chunk.getContent();
        this.compactContent = chunk.getCompactContent();
        this.subContent = chunk.getSubContent();
        this.tokenSize = chunk.getTokenSize();
        this.compactTokenSize = chunk.getCompactTokenSize();
        return this;
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
                .compactTokenSize(compactTokenSize)
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
                .compactContent(chunk.getCompactContent())
                .subContent(chunk.getSubContent())
                .tokenSize(chunk.getTokenSize())
                .compactTokenSize(chunk.getCompactTokenSize())
                .build();
    }
}
