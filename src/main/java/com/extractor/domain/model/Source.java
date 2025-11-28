package com.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;
}
