package com.genai.extractor.application.command;

import com.genai.common.vo.UploadFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ExtractFileCommand {

    private UploadFile file;

    private String extractType;
}
