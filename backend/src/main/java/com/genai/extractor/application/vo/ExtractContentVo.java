package com.genai.extractor.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ExtractContentVo {

    private final String type;

    private final String content;
}
