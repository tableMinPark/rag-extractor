package com.extractor.domain.model;

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

    private final String version;

    private final String sourceType;

    private final String categoryCode;

    private final String name;

    private final String content;

    private final String collectionId;

    private final Long fileDetailId;

    private final Integer maxTokenSize;

    private final Integer overlapSize;

    private final Boolean isActive;

    private final LocalDateTime sysCreateDt;

    private final LocalDateTime sysModifyDt;

    private final List<SourcePattern> sourcePatterns;

    private final List<SourceStopPattern> sourceStopPatterns;
}
