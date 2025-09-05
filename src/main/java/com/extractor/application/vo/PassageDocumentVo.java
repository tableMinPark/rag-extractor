package com.extractor.application.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PassageDocumentVo {

    private final String docId;

    private final int depth;

    private final int tokenSize;

    private final String fullTitle;

    private final String[] titles;

    private final String content;

    @Builder
    public PassageDocumentVo(String docId, int depth, int tokenSize, String fullTitle, String[] titles, String content) {
        this.docId = docId;
        this.depth = depth;
        this.tokenSize = tokenSize;
        this.fullTitle = fullTitle;
        this.titles = titles;
        this.content = content;
    }
}
