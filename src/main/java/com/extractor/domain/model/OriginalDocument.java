package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class OriginalDocument {

    private String fileName;

    private String originalFileName;

    private Path path;

    private Path fullPath;

    private String extension;

    @Builder
    public OriginalDocument(String fileName, String originalFileName, Path path, Path fullPath, String extension) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.path = path;
        this.fullPath = fullPath;
        this.extension = extension;
    }
}
