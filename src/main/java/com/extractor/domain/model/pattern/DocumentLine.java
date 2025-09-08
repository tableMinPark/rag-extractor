package com.extractor.domain.model.pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class DocumentLine {

    private final String content;

    @Setter
    private String prefix;

    private final LineType type;

    @Builder
    public DocumentLine(LineType type, String content) {
        this.type = type;
        this.content = content;
        this.prefix = "";
    }

    public String getSimpleContent() {
        String content = this.content;
        if (this.prefix != null && !this.prefix.isBlank()) {
            return content.replaceFirst(this.prefix, "").trim();
        } else {
            return content;
        }
    }

    public enum LineType {
        TEXT, TABLE, IMAGE,
    }
}
