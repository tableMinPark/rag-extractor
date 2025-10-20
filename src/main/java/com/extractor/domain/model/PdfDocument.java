package com.extractor.domain.model;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Arrays;

@ToString
@Getter
public class PdfDocument extends Document {

    private final String content;

    @Builder
    public PdfDocument(String name, FileExtension extension, Path path, String content) {
        super(name, extension, path);
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