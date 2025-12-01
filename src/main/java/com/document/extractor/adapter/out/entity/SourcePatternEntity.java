package com.document.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE_PATTERN")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_PATTERN_SOURCE_PATTERN_ID_SEQ",
        sequenceName = "WN_SOURCE_PATTERN_SOURCE_PATTERN_ID_SEQ",
        allocationSize = 1
)
public class SourcePatternEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_PATTERN_SOURCE_PATTERN_ID_SEQ")
    @Column(name = "source_pattern_id", nullable = false)
    private Long sourcePatternId;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "token_size")
    private Integer tokenSize;

    @Column(name = "depth")
    private Integer depth;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source_pattern_id")
    private List<SourcePrefixEntity> sourcePrefixes;
}
