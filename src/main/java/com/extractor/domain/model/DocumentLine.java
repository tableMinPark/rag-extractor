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

    @Setter
    private String prefix;

    @Builder
    public DocumentLine(LineType type, String content) {
        this.type = type;
        this.content = content;
        this.prefix = "";
    }

    public enum LineType {
        TEXT, TABLE, IMAGE,
    }
}
