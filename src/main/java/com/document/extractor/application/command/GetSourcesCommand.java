package com.document.extractor.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetSourcesCommand {

    private final int page;

    private final int size;

    private final boolean isActive;
}
