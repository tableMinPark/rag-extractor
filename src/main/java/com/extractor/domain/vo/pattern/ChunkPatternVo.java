package com.extractor.domain.vo.pattern;

import lombok.Getter;

import java.util.List;

@Getter
public class ChunkPatternVo {

    private int depthSize;

    private List<PatternVo> patterns;

    private List<PatternVo> stopPatterns;

    public ChunkPatternVo(List<PatternVo> patterns, List<PatternVo> stopPatterns) {
        this.patterns = patterns;
        this.stopPatterns = stopPatterns;

        int maxDepth = 0;
        for (PatternVo pattern : patterns) {
            maxDepth = Math.max(maxDepth, pattern.getDepth());
        }

        this.depthSize = maxDepth + 1;
    }
}
