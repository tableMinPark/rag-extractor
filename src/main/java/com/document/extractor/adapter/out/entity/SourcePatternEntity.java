package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.SourcePattern;
import com.document.extractor.domain.model.SourcePrefix;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE_PATTERN")
@Comment("대상 문서 패턴")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_PATTERN_ID_SEQ",
        sequenceName = "WN_SOURCE_PATTERN_ID_SEQ",
        allocationSize = 1
)
public class SourcePatternEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_PATTERN_ID_SEQ")
    @Column(name = "source_pattern_id", nullable = false, updatable = false)
    @Comment("전처리 패턴 ID")
    private Long sourcePatternId;

    @Column(name = "source_id")
    @Comment("대상 문서 ID")
    private Long sourceId;

    @Column(name = "token_size")
    @Comment("패턴 최대 토큰 크기")
    private Integer tokenSize;

    @Column(name = "depth")
    @Comment("패턴 레벨")
    private Integer depth;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source_pattern_id")
    private List<SourcePrefixEntity> sourcePrefixes;

    public void update(SourcePattern sourcePattern) {
        this.sourceId = sourcePattern.getSourceId();
        this.tokenSize = sourcePattern.getTokenSize();
        this.depth = sourcePattern.getDepth();

        for (SourcePrefix sourcePrefix : sourcePattern.getSourcePrefixes()) {
            Optional<SourcePrefixEntity> sourcePrefixEntityOptional = this.findSourcePrefix(sourcePrefix.getSourcePrefixId());

            if (sourcePrefixEntityOptional.isPresent()) {
                sourcePrefixEntityOptional.get().update(sourcePrefix);
                break;
            }
        }
    }

    public Optional<SourcePrefixEntity> findSourcePrefix(Long sourcePrefixId) {
        for (SourcePrefixEntity sourcePrefix : sourcePrefixes) {
            if (sourcePrefixId.equals(sourcePrefix.getSourcePrefixId())) {
                return Optional.of(sourcePrefix);
            }
        }
        return Optional.empty();
    }

    public SourcePattern toDomain() {
        return SourcePattern.builder()
                .sourcePatternId(sourcePatternId)
                .sourceId(sourceId)
                .tokenSize(tokenSize)
                .depth(depth)
                .sourcePrefixes(sourcePrefixes.stream()
                        .map(SourcePrefixEntity::toDomain)
                        .sorted(Comparator.comparingInt(SourcePrefix::getOrder))
                        .toList())
                .build();
    }

    public static SourcePatternEntity fromDomain(SourcePattern sourcePattern) {
        return SourcePatternEntity.builder()
                .sourceId(sourcePattern.getSourceId())
                .tokenSize(sourcePattern.getTokenSize())
                .depth(sourcePattern.getDepth())
                .sourcePrefixes(sourcePattern.getSourcePrefixes().stream()
                        .map(SourcePrefixEntity::fromDomain)
                        .toList())
                .build();
    }
}
