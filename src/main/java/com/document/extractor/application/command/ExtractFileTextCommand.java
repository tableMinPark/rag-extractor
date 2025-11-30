package com.document.extractor.application.command;

import com.document.extractor.application.vo.FileVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ExtractFileTextCommand {

    private FileVo file;
}
