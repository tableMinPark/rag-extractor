package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OriginalDocument {

    private final Long originalId;

    private final String docId;

    private final String docType;

    private final String categoryCode;

    private final String name;

    private final String filePath;

    private final String content;

    @Builder
    public OriginalDocument(Long originalId, String docId, String docType, String categoryCode, String name, String filePath, String content) {
        this.originalId = originalId;
        this.docId = docId;
        this.docType = docType;
        this.categoryCode = categoryCode;
        this.name = name;
        this.filePath = filePath == null ? "" : filePath;
        this.content = content == null ? "" : content;
    }
}
