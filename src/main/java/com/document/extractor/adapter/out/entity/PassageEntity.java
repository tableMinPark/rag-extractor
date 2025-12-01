package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.Passage;
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
@Table(name = "WN_PASSAGE")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_PASSAGE_PASSAGE_ID_SEQ",
        sequenceName = "WN_PASSAGE_PASSAGE_ID_SEQ",
        allocationSize = 1
)
public class PassageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_PASSAGE_PASSAGE_ID_SEQ")
    @Column(name = "passage_id", nullable = false, updatable = false)
    private Long passageId;

    @Column(name = "source_id")
    private Long sourceId;

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

    public void update(Passage passage) {
        this.sourceId = passage.getSourceId();
        this.version = passage.getVersion();
        this.title = passage.getTitle();
        this.subTitle = passage.getSubTitle();
        this.thirdTitle = passage.getThirdTitle();
        this.content = passage.getContent();
        this.subContent = passage.getSubContent();
        this.tokenSize = passage.getTokenSize();
    }

    public Passage toDomain() {
        return Passage.builder()
                .passageId(passageId)
                .sourceId(sourceId)
                .version(version)
                .title(title)
                .subTitle(subTitle)
                .thirdTitle(thirdTitle)
                .content(content)
                .subContent(subContent)
                .tokenSize(tokenSize)
                .sysCreateDt(sysCreateDt)
                .sysModifyDt(sysModifyDt)
                .build();
    }

    public static PassageEntity fromDomain(Passage passage) {
        return PassageEntity.builder()
                .passageId(passage.getPassageId())
                .sourceId(passage.getSourceId())
                .version(passage.getVersion())
                .title(passage.getTitle())
                .subTitle(passage.getSubTitle())
                .thirdTitle(passage.getThirdTitle())
                .content(passage.getContent())
                .subContent(passage.getSubContent())
                .tokenSize(passage.getTokenSize())
                .build();
    }
}
