package com.extractor.domain.vo.document;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class OriginalDocumentVo {

    private final String fileName;

    private final String originalFileName;

    private final Path path;

    private final Path fullPath;

    private final String extension;

    @Builder
    public OriginalDocumentVo(String fileName, String originalFileName, Path path, Path fullPath, String extension) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.path = path;
        this.fullPath = fullPath;
        this.extension = extension;
    }
}
