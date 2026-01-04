package com.document.extractor.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class GetSourcesCommand {

    private final int page;

    private final int size;

    private final String orderBy;

    private final String order;

    private final String keyword;

    private final String categoryCode;
}
