package com.extractor.application.vo;

import com.extractor.domain.vo.PatternVo;
import lombok.Getter;

import java.util.List;

@Getter
public class ChunkPatternVo {

    private final List<PatternVo> patterns;

    private final List<String> antiPatterns;

    private final int maxTokenSize;

    private final int overlapSize;

    public ChunkPatternVo(List<PatternVo> patterns, List<String> antiPatterns, int maxTokenSize, int overlapSize) {
        this.patterns = patterns;
        this.antiPatterns = antiPatterns;
        this.maxTokenSize = maxTokenSize;
        this.overlapSize = overlapSize;
    }
}
