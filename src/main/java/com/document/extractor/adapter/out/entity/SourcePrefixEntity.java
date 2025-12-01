package com.document.extractor.adapter.out.entity;

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
    @Column(name = "source_prefix_id", nullable = false)
    private Long sourcePrefixId;

    @Column(name = "source_pattern_id")
    private Long sourcePatternId;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "sort_order")
    private Integer order;

    @Column(name = "is_title")
    private Boolean isTitle;
}
