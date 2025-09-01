package com.extractor.domain.model;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class OriginalDocument {

    private final String docId;

    private final String originalFileName;

    private final Path path;

    private final Path fullPath;

    private final FileExtension extension;

    @Builder
    public OriginalDocument(String docId, String originalFileName, Path path, Path fullPath, FileExtension extension) {
        this.docId = docId;
        this.originalFileName = originalFileName;
        this.path = path;
        this.fullPath = fullPath;
        this.extension = extension;
    }
}
