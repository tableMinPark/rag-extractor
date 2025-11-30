package com.document.extractor.application.command;

import com.document.extractor.application.vo.FileVo;
import com.document.global.enums.ExtractType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ExtractFileCommand {

    private FileVo file;

    private ExtractType extractType;
}
