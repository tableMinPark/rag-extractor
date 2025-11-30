package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class SourcePattern {

    private final Long sourcePatternId;

    private final Long sourceId;

    private final Integer tokenSize;

    private final Integer depth;

    private List<SourcePrefix> sourcePrefixes;
}
