package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class SourcePrefix {

    private final Long sourcePrefixId;

    private final Long sourcePatternId;

    private final String prefix;

    private final Integer order;

    private final Boolean isTitle;
}
