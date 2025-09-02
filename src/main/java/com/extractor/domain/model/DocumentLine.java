package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class DocumentLine {
    
    private LineType type;

    private String content;

    private int sum;

    @Setter
    private String prefix;

    @Builder
    public DocumentLine(LineType type, String content, int sum) {
        this.type = type;
        this.content = content;
        this.prefix = "";
        this.sum = sum;
    }

    public enum LineType {
        TEXT, TABLE, IMAGE,
    }
}
