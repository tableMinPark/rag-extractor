package com.document.extractor.domain.model;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class Source {

    private final Long sourceId;

    private Long version;

    private final SourceType sourceType;

    private final SelectType selectType;

    private final String categoryCode;

    private final String name;

    private final String content;

    private final String collectionId;

    private final Long fileDetailId;

    private final Integer maxTokenSize;

    private final Integer overlapSize;

    private final Boolean isAuto;

    private final LocalDateTime sysCreateDt;

    private final LocalDateTime sysModifyDt;

    private final List<SourcePattern> sourcePatterns;

    private final List<SourceStopPattern> sourceStopPatterns;

    /**
     * 대상 문서 버전 초기화
     */
    public void increaseVersion() {
        if (this.version == null) {
            this.version = 1L;
        } else {
            this.version = this.version + 1;
        }
    }
}
