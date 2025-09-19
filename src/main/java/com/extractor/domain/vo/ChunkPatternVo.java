package com.extractor.domain.vo;

import lombok.Getter;

import java.util.List;

@Getter
public class ChunkPatternVo {

    private final List<PatternVo> patterns;

    private final List<String> antiPatterns;

    public ChunkPatternVo(List<PatternVo> patterns, List<String> antiPatterns) {
        this.patterns = patterns;
        this.antiPatterns = antiPatterns;
    }
}
