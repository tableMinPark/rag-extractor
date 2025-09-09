package com.extractor.application.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ExtractContentVo {

    private final String type;

    private final String content;

    @Builder
    public ExtractContentVo(String type, String content) {
        this.type = type;
        this.content = content;
    }
}
