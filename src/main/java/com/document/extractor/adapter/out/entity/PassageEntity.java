package com.document.extractor.adapter.out.entity;

import com.document.extractor.application.enums.UpdateState;
import com.document.extractor.domain.model.Passage;
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
@Table(name = "WN_PASSAGE")
@Comment("패시지")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_PASSAGE_ID_SEQ",
        sequenceName = "WN_PASSAGE_ID_SEQ",
        allocationSize = 1
)
public class PassageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_PASSAGE_ID_SEQ")
    @Column(name = "passage_id", nullable = false, updatable = false)
    @Comment("패시지 ID")
    private Long passageId;

    @Column(name = "source_id")
    @Comment("대상 문서 ID")
    private Long sourceId;

    @Column(name = "version")
    @Comment("버전 코드")
    private Long version;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "title", length = 4000)
    @Comment("제목")
    private String title;

    @Column(name = "sub_title", length = 4000)
    @Comment("중제목")
    private String subTitle;

    @Column(name = "third_title", length = 4000)
    @Comment("소제목")
    private String thirdTitle;

    @Lob
    @Column(name = "content")
    @Comment("본문")
    private String content;

    @Lob
    @Column(name = "sub_content")
    @Comment("부가 본문")
    private String subContent;

    @Column(name = "token_size")
    @Comment("본문 토큰 크기")
    private Integer tokenSize;

    @Column(name = "update_state")
    @Comment("변경 이력 코드")
    private String updateStateCode;

    @Column(name = "parent_sort_order")
    @Comment("부모 정렬 필드")
    private Integer parentSortOrder;

    @CreatedDate
    @Column(name = "sys_create_dt")
    @Comment("생성 일자")
    private LocalDateTime sysCreateDt;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    @Comment("수정 일자")
    private LocalDateTime sysModifyDt;

    public PassageEntity update(Passage passage) {
        this.sourceId = passage.getSourceId();
        this.version = passage.getVersion();
        this.sortOrder = passage.getSortOrder();
        this.parentSortOrder = passage.getParentSortOrder();
        this.title = passage.getTitle();
        this.subTitle = passage.getSubTitle();
        this.thirdTitle = passage.getThirdTitle();
        this.content = passage.getContent();
        this.subContent = passage.getSubContent();
        this.tokenSize = passage.getTokenSize();
        this.updateStateCode = passage.getUpdateState().getCode();
        return this;
    }

    public Passage toDomain() {
        return Passage.builder()
                .passageId(passageId)
                .sourceId(sourceId)
                .version(version)
                .sortOrder(sortOrder)
                .parentSortOrder(parentSortOrder)
                .title(title == null ? "" : title)
                .subTitle(subTitle == null ? "" : subTitle)
                .thirdTitle(thirdTitle == null ? "" : thirdTitle)
                .content(content == null ? "" : content)
                .subContent(subContent == null ? "" : subContent)
                .tokenSize(tokenSize)
                .updateState(UpdateState.find(updateStateCode))
                .sysCreateDt(sysCreateDt)
                .sysModifyDt(sysModifyDt)
                .build();
    }

    public static PassageEntity fromDomain(Passage passage) {
        return PassageEntity.builder()
                .passageId(passage.getPassageId())
                .sourceId(passage.getSourceId())
                .version(passage.getVersion())
                .sortOrder(passage.getSortOrder())
                .parentSortOrder(passage.getParentSortOrder())
                .title(passage.getTitle())
                .subTitle(passage.getSubTitle())
                .thirdTitle(passage.getThirdTitle())
                .content(passage.getContent())
                .subContent(passage.getSubContent())
                .tokenSize(passage.getTokenSize())
                .updateStateCode(passage.getUpdateState().getCode())
                .build();
    }
}
