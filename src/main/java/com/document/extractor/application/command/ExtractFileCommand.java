package com.document.extractor.application.command;

import com.document.global.vo.UploadFile;
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
