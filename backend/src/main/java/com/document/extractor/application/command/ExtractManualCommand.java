package com.document.extractor.application.command;

import com.document.extractor.application.enums.ExtractType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ExtractManualCommand {

    private final String manualId;

    private final ExtractType extractType;
}
