package com.extractor.domain.vo.pattern;

import lombok.Getter;

import java.util.List;

@Getter
public class ChunkPatternVo {

    private final int tokenSize;

    private final int depthSize;

    private final List<PatternVo> patterns;

    private final List<String> stopPatterns;

    public ChunkPatternVo(int tokenSize, List<PatternVo> patterns, List<String> stopPatterns) {
        this.tokenSize = tokenSize;
        this.patterns = patterns;
        this.stopPatterns = stopPatterns;

        int maxDepth = 0;
        for (PatternVo pattern : patterns) {
            maxDepth = Math.max(maxDepth, pattern.getDepth());
        }

        this.depthSize = maxDepth + 1;
    }
}
