package com.document.extractor.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class GetPassagesCommand {

    private final long sourceId;

    private final int page;

    private final int size;
}
