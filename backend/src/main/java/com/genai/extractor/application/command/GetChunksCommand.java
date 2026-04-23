package com.genai.extractor.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class GetChunksCommand {

    private final long passageId;

    private final int page;

    private final int size;
}
