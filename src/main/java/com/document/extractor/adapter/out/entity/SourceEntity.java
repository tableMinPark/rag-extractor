package com.document.extractor.adapter.out.entity;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.domain.model.Source;
import com.document.extractor.domain.model.SourcePattern;
import com.document.extractor.domain.model.SourceStopPattern;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
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
@Comment("대상 문서")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_SOURCE_ID_SEQ",
        sequenceName = "WN_SOURCE_ID_SEQ",
        allocationSize = 1
)
public class SourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_SOURCE_ID_SEQ")
    @Column(name = "source_id", nullable = false, updatable = false)
    @Comment("대상 문서 ID")
    private Long sourceId;

    @Column(name = "version")
    @Comment("버전 코드")
    private Long version;

    @Column(name = "source_type")
    @Comment("대상 문서 타입")
    private String sourceType;

    @Column(name = "select_type")
    @Comment("전처리 타입")
    private String selectType;

    @Column(name = "category_code")
    @Comment("대상 문서 분류")
    private String categoryCode;

    @Column(name = "name")
    @Comment("대상 문서명")
    private String name;

//    @Lob
    @Column(name = "content")
    @Comment("대상 문서 본문")
    private String content;

    @Column(name = "collection_id")
    @Comment("색인 테이블 ID")
    private String collectionId;

    @Column(name = "file_detail_id")
    @Comment("파일 상세 ID")
    private Long fileDetailId;

    @Column(name = "max_token_size")
    @Comment("전처리 최대 토큰 크기")
    private Integer maxTokenSize;

    @Column(name = "overlap_size")
    @Comment("전처리 오버랩 크기")
    private Integer overlapSize;

    @Column(name = "is_auto")
    @Comment("자동화 처리 여부")
    private Boolean isAuto;

    @CreatedDate
    @Column(name = "sys_create_dt")
    @Comment("생성 일자")
    private LocalDateTime sysCreateDt;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    @Comment("수정 일자")
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
        this.isAuto = source.getIsAuto();

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
                .isAuto(isAuto)
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
                .isAuto(source.getIsAuto())
                .sourcePatterns(source.getSourcePatterns().stream()
                        .map(SourcePatternEntity::fromDomain)
                        .toList())
                .sourceStopPatterns(source.getSourceStopPatterns().stream()
                        .map(SourceStopPatternEntity::fromDomain)
                        .toList())
                .build();
    }
}
