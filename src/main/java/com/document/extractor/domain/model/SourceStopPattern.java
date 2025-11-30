package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class SourceStopPattern {

    private final Long sourceStopPatternId;

    private final Long sourceId;

    private final String prefix;
}
