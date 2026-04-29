package com.genai.extractor.service.domain.model;

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
