package com.extractor.chunk.adapter.in.dto.etc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatternDto {

    @Schema(description = "제약 토큰 수")
    private int tokenSize;

    @Schema(description = "정규식 정보")
    private List<PrefixDto> prefixes;
}
