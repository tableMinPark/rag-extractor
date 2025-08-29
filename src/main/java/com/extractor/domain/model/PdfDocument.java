package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Arrays;

@ToString
@Getter
public class PdfDocument extends ExtractDocument {

    private String content;

    @Builder
    public PdfDocument(String docId, String name, Path path, String content) {
        super(docId, name, path);
        this.content = content;
    }

    /**
     * 추출
     */
    public void extract() {
        Arrays.stream(this.content.split("\n")).forEach(super::addText);
    }
}
