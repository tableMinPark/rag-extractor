package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.SourceStopPattern;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE_STOP_PATTERN")
@Comment("대상 문서 중단 패턴")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_STOP_PATTERN_ID_SEQ",
        sequenceName = "WN_SOURCE_STOP_PATTERN_ID_SEQ",
        allocationSize = 1
)
public class SourceStopPatternEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_STOP_PATTERN_ID_SEQ")
    @Column(name = "source_stop_pattern_id", nullable = false, updatable = false)
    @Comment("전처리 중단 패턴 ID")
    private Long sourceStopPatternId;

    @Column(name = "source_id")
    @Comment("대상 문서 ID")
    private Long sourceId;

    @Column(name = "prefix")
    @Comment("전처리 중단 패턴 정규식")
    private String prefix;

    public void update(SourceStopPattern sourceStopPattern) {
        this.sourceId = sourceStopPattern.getSourceId();
        this.prefix = sourceStopPattern.getPrefix();
    }

    public SourceStopPattern toDomain() {
        return SourceStopPattern.builder()
                .sourceStopPatternId(sourceStopPatternId)
                .sourceId(sourceId)
                .prefix(prefix == null ? "" : prefix)
                .build();
    }

    public static SourceStopPatternEntity fromDomain(SourceStopPattern sourceStopPattern) {
        return SourceStopPatternEntity.builder()
                .sourceId(sourceStopPattern.getSourceId())
                .prefix(sourceStopPattern.getPrefix())
                .build();
    }
}
