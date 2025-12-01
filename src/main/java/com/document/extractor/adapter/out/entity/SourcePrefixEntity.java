package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.SourcePrefix;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE_PREFIX")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_PREFIX_SOURCE_PREFIX_ID_SEQ",
        sequenceName = "WN_SOURCE_PREFIX_SOURCE_PREFIX_ID_SEQ",
        allocationSize = 1
)
public class SourcePrefixEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_PREFIX_SOURCE_PREFIX_ID_SEQ")
    @Column(name = "source_prefix_id", nullable = false, updatable = false)
    private Long sourcePrefixId;

    @Column(name = "source_pattern_id")
    private Long sourcePatternId;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "sort_order")
    private Integer order;

    @Column(name = "is_title")
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
                .prefix(prefix)
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
