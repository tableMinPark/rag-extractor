package com.document.extractor.domain.model;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
public class Source {

    @Getter
    private final Long sourceId;

    private Long version;

    @Getter
    private final SourceType sourceType;

    @Getter
    private final SelectType selectType;

    @Getter
    private final String categoryCode;

    @Getter
    private final String name;

    @Getter
    private final String content;

    @Getter
    private final String collectionId;

    @Getter
    private final Long fileDetailId;

    @Getter
    private final Integer maxTokenSize;

    @Getter
    private final Integer overlapSize;

    @Getter
    private final Boolean isAuto;

    @Getter
    private final LocalDateTime sysCreateDt;

    @Getter
    private final LocalDateTime sysModifyDt;

    @Getter
    private final List<SourcePattern> sourcePatterns;

    @Getter
    private final List<SourceStopPattern> sourceStopPatterns;

    @Builder.Default
    private int increaseVersion = 0;

    /**
     * 대상 문서 버전 초기화
     */
    public void nextVersion() {
        this.increaseVersion = 1;
    }

    /**
     * 현재 버전 조회
     *
     * @return 현재 버전
     */
    public Long getVersion() {
        return version + increaseVersion;
    }

    /**
     * 첫번째 버전 여부
     *
     * @return 첫번째 버전 여부
     */
    public boolean isFirstVersion() {
        return this.getVersion() == 1L;
    }

    /**
     * 이전 버전 코드 조회
     *
     * @return 이전 버전 코드
     */
    public long getPreviousVersion() {
        return this.getVersion() - 1;
    }
}
