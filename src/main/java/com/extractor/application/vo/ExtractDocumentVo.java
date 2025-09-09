package com.extractor.application.vo;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ExtractDocumentVo {

    private final String docId;

    private final String name;

    private final FileExtension extension;

    private final List<ExtractContentVo> extractContents;

    @Builder
    public ExtractDocumentVo(String docId, String name, FileExtension extension, List<ExtractContentVo> extractContents) {
        this.docId = docId;
        this.name = name;
        this.extension = extension;
        this.extractContents = extractContents;
    }
}
