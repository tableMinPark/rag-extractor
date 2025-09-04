package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class DocumentLine {
    
    private final LineType type;

    private final String content;

    @Setter
    private String prefix;

    @Builder
    public DocumentLine(LineType type, String content) {
        this.type = type;
        this.content = content;
        this.prefix = "";
    }

    public String getContent() {
        if (this.prefix != null && !this.prefix.isBlank()) {
            return this.content.replaceFirst(this.prefix, "");
        } else {
            return this.content;
        }
    }

    public enum LineType {
        TEXT, TABLE, IMAGE,
    }
}
