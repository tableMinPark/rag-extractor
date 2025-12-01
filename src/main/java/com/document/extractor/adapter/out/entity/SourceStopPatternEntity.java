package com.document.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE_STOP_PATTERN")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_STOP_PATTERN_SOURCE_STOP_PATTERN_ID_SEQ",
        sequenceName = "WN_SOURCE_STOP_PATTERN_SOURCE_STOP_PATTERN_ID_SEQ",
        allocationSize = 1
)
public class SourceStopPatternEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_STOP_PATTERN_SOURCE_STOP_PATTERN_ID_SEQ")
    @Column(name = "source_stop_pattern_id", nullable = false)
    private Long sourceStopPatternId;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "prefix")
    private String prefix;
}
