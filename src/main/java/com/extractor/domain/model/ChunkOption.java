package com.extractor.domain.model;

import com.extractor.domain.vo.PatternVo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class ChunkOption {

    public enum ChunkType {
        EQUALS, REGEX, NONE,
    }

    private final int maxTokenSize;

    private final int overlapSize;

    private final int depthSize;

    private final List<PatternVo> patterns;

    private final ChunkType type;

    @Builder
    public ChunkOption(int maxTokenSize, int overlapSize, List<PatternVo> patterns, ChunkType type) {
        this.maxTokenSize = maxTokenSize;
        this.overlapSize = overlapSize;
        this.patterns = patterns == null ? new ArrayList<>() : patterns;
        this.depthSize = this.patterns.size();
        this.type = type;
    }
}
