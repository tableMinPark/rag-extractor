package com.extractor.extract.domain.model;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Arrays;

@ToString
@Getter
public class ExtractPdfDocument extends ExtractDocument {

    private final String content;

    @Builder
    public ExtractPdfDocument(String name, FileExtension extension, Path path, String content) {
        super(name, extension, path);
        this.content = content;
    }

    /**
     * 추출
     */
    public void extract() {
        Arrays.stream(this.content.split("\n")).forEach(super::addTextContent);
    }
}