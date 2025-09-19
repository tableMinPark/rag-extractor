package com.extractor.application.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OriginalDocumentVo {

    private final Long originalId;

    private final String version;

    private final String docType;

    private final String categoryCode;

    private final String name;

    private final String filePath;

    private final String content;

    @Builder
    public OriginalDocumentVo(Long originalId, String version, String docType, String categoryCode, String name, String filePath, String content) {
        this.originalId = originalId;
        this.version = version;
        this.docType = docType;
        this.categoryCode = categoryCode;
        this.name = name;
        this.filePath = filePath;
        this.content = content;
    }
}
