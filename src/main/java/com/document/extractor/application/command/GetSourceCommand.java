package com.document.extractor.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetSourceCommand {

    private final long sourceId;
}
