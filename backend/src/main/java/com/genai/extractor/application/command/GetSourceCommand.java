package com.genai.extractor.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class GetSourceCommand {

    private final long sourceId;
}
