package com.document.extractor.adapter.out.entity;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.domain.model.Source;
import com.document.extractor.domain.model.SourcePattern;
import com.document.extractor.domain.model.SourceStopPattern;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_SOURCE")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_SOURCE_ID_SEQ",
        sequenceName = "WN_SOURCE_SOURCE_ID_SEQ",
        allocationSize = 1
)
public class SourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_SOURCE_ID_SEQ")
    @Column(name = "source_id", nullable = false, updatable = false)
    private Long sourceId;

    @Column(name = "version")
    private Long version;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "select_type")
    private String selectType;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "name")
    private String name;

    @Column(name = "content")
    private String content;

    @Column(name = "collection_id")
    private String collectionId;

    @Column(name = "file_detail_id")
    private Long fileDetailId;

    @Column(name = "max_token_size")
    private Integer maxTokenSize;

    @Column(name = "overlap_size")
    private Integer overlapSize;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreatedDate
    @Column(name = "sys_create_dt")
    private LocalDateTime sysCreateDt;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    private LocalDateTime sysModifyDt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source_id")
    private List<SourcePatternEntity> sourcePatterns;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source_id")
    private List<SourceStopPatternEntity> sourceStopPatterns;

    public void update(Source source) {
        this.version = source.getVersion();
        this.sourceType = source.getSourceType().getCode();
        this.selectType = source.getSelectType().getCode();
        this.categoryCode = source.getCategoryCode();
        this.name = source.getName();
        this.content = source.getContent();
        this.collectionId = source.getCollectionId();
        this.fileDetailId = source.getFileDetailId();
        this.maxTokenSize = source.getMaxTokenSize();
        this.overlapSize = source.getOverlapSize();
        this.isActive = source.getIsActive();

        for (SourcePattern sourcePattern : source.getSourcePatterns()) {
            Optional<SourcePatternEntity> sourcePatternEntityOptional = this.getSourcePattern(sourcePattern.getSourcePatternId());

            if (sourcePatternEntityOptional.isPresent()) {
                sourcePatternEntityOptional.get().update(sourcePattern);
                break;
            }
        }

        for (SourceStopPattern sourceStopPattern : source.getSourceStopPatterns()) {
            Optional<SourceStopPatternEntity> sourceStopPatternEntityOptional = this.getSourceStopPattern(sourceStopPattern.getSourceStopPatternId());

            if (sourceStopPatternEntityOptional.isPresent()) {
                sourceStopPatternEntityOptional.get().update(sourceStopPattern);
                break;
            }
        }
    }

    public Optional<SourcePatternEntity> getSourcePattern(Long sourcePatternId) {
        for (SourcePatternEntity sourcePattern : sourcePatterns) {
            if (sourcePatternId.equals(sourcePattern.getSourcePatternId())) {
                return Optional.of(sourcePattern);
            }
        }
        return Optional.empty();
    }

    public Optional<SourceStopPatternEntity> getSourceStopPattern(Long sourceStopPatternId) {
        for (SourceStopPatternEntity sourceStopPattern : sourceStopPatterns) {
            if (sourceStopPatternId.equals(sourceStopPattern.getSourceStopPatternId())) {
                return Optional.of(sourceStopPattern);
            }
        }
        return Optional.empty();
    }

    public Source toDomain() {
        return Source.builder()
                .sourceId(sourceId)
                .version(version)
                .sourceType(SourceType.find(sourceType))
                .selectType(SelectType.find(selectType))
                .categoryCode(categoryCode)
                .name(name)
                .content(content)
                .collectionId(collectionId)
                .fileDetailId(fileDetailId)
                .maxTokenSize(maxTokenSize)
                .overlapSize(overlapSize)
                .isActive(isActive)
                .sysCreateDt(sysCreateDt)
                .sysModifyDt(sysModifyDt)
                .sourcePatterns(sourcePatterns.stream()
                        .map(SourcePatternEntity::toDomain)
                        .toList())
                .sourceStopPatterns(sourceStopPatterns.stream()
                        .map(SourceStopPatternEntity::toDomain)
                        .toList())
                .build();
    }

    public static SourceEntity fromDomain(Source source) {
        return SourceEntity.builder()
                .sourceId(source.getSourceId())
                .version(source.getVersion())
                .sourceType(source.getSourceType().getCode())
                .selectType(source.getSelectType().getCode())
                .categoryCode(source.getCategoryCode())
                .name(source.getName())
                .content(source.getContent())
                .collectionId(source.getCollectionId())
                .fileDetailId(source.getFileDetailId())
                .maxTokenSize(source.getMaxTokenSize())
                .overlapSize(source.getOverlapSize())
                .isActive(source.getIsActive())
                .sourcePatterns(source.getSourcePatterns().stream()
                        .map(SourcePatternEntity::fromDomain)
                        .toList())
                .sourceStopPatterns(source.getSourceStopPatterns().stream()
                        .map(SourceStopPatternEntity::fromDomain)
                        .toList())
                .build();
    }
}
