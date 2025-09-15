package com.extractor.chunk.application.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChunkVo {

    private final int depth;

    private final int tokenSize;

    private final String[] titles;

    private final String fullTitle;

    private final String content;

    private final String subContent;

    @Builder
    public ChunkVo(int depth, int tokenSize, String[] titles, String fullTitle, String content, String subContent) {
        this.depth = depth;
        this.tokenSize = tokenSize;
        this.titles = titles;
        this.fullTitle = fullTitle;
        this.content = content;
        this.subContent = subContent;
    }
}
