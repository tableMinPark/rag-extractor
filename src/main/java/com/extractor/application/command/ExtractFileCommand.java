package com.extractor.application.command;

import com.extractor.application.vo.FileVo;
import com.extractor.application.enums.ExtractType;
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
