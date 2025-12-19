package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.SourcePrefix;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE_PREFIX")
@Comment("대상 문서 패턴 정규식")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_PREFIX_ID_SEQ",
        sequenceName = "WN_SOURCE_PREFIX_ID_SEQ",
        allocationSize = 1
)
public class SourcePrefixEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_PREFIX_ID_SEQ")
    @Column(name = "source_prefix_id", nullable = false, updatable = false)
    @Comment("패턴 정규식 ID")
    private Long sourcePrefixId;

    @Column(name = "source_pattern_id")
    @Comment("전처리 패턴 ID")
    private Long sourcePatternId;

    @Column(name = "prefix")
    @Comment("전처리 패턴 정규식")
    private String prefix;

    @Column(name = "sort_order")
    @Comment("정렬 기준")
    private Integer order;

    @Column(name = "is_title")
    @Comment("제목 추출 여부")
    private Boolean isTitle;

    public void update(SourcePrefix sourcePrefix) {
        this.sourcePatternId = sourcePrefix.getSourcePatternId();
        this.prefix = sourcePrefix.getPrefix();
        this.order = sourcePrefix.getOrder();
        this.isTitle = sourcePrefix.getIsTitle();
    }

    public SourcePrefix toDomain() {
        return SourcePrefix.builder()
                .sourcePrefixId(sourcePrefixId)
                .sourcePatternId(sourcePatternId)
                .prefix(prefix == null ? "" : prefix)
                .order(order)
                .isTitle(isTitle)
                .build();
    }

    public static SourcePrefixEntity fromDomain(SourcePrefix sourcePrefix) {
        return SourcePrefixEntity.builder()
                .prefix(sourcePrefix.getPrefix())
                .order(sourcePrefix.getOrder())
                .isTitle(sourcePrefix.getIsTitle())
                .build();
    }
}
