package com.extractor.application.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DocumentLineVo {

    private final String type;

    private final String content;

    @Builder
    public DocumentLineVo(String type, String content) {
        this.type = type;
        this.content = content;
    }
}
