package com.extractor.extract.domain.model;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FileDocument {

    private final String originalFileName;

    private final Path path;

    private final Path fullPath;

    private final FileExtension extension;

    @Builder
    public FileDocument(String originalFileName, Path path, Path fullPath, FileExtension extension) {
        this.originalFileName = originalFileName;
        this.path = path;
        this.fullPath = fullPath;
        this.extension = extension;
    }
}
