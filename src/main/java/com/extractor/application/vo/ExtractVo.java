package com.extractor.application.vo;

import com.extractor.global.enums.FileExtension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ExtractVo {

    private final String name;

    private final FileExtension extension;

    private final List<ExtractContentVo> extractContents;
}
