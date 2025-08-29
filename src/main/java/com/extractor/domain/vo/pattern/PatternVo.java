package com.extractor.domain.vo.pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PatternVo {

    @Schema(description = "제목 번호")
    private final int depth;

    @Schema(description = "정규식")
    private final String prefix;

    public PatternVo(int depth, String prefix) {
        this.depth = depth;
        this.prefix = prefix;
    }
}
