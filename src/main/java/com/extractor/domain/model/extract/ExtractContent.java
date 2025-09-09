package com.extractor.domain.model.extract;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class ExtractContent {

    private final String content;

    private final LineType type;

    @Setter
    private String prefix;

    @Builder
    public ExtractContent(String content, LineType type) {
        this.content = content;
        this.type = type;
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
