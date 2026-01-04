package com.document.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
@Getter
public class PdfDocument extends Document {

    private final String content;

    @Builder
    public PdfDocument(String name, String content, boolean convertError) {
        super(name, convertError);
        this.content = content;
        this.extract();
    }

    /**
     * 추출
     */
    public void extract() {
        this.clearDocumentContents();
        Arrays.stream(this.content.split("\n")).forEach(super::addTextContent);
    }
}