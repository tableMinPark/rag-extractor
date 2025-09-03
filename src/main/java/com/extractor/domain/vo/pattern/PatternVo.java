package com.extractor.domain.vo.pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PatternVo {

    @Schema(description = "제목 번호")
    private final int depth;

    @Schema(description = "제약 토큰 수")
    private final int tokenSize;

    @Schema(description = "정규식")
    private final String prefix;

    public PatternVo(int depth, int tokenSize, String prefix) {
        this.depth = depth;
        this.tokenSize = tokenSize;
        this.prefix = prefix;
    }
}
