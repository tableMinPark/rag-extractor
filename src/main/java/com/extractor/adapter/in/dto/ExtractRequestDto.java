package com.extractor.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "전처리 패턴 정보")
public class ExtractRequestDto {

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\",\"^부(\\\\s+)?칙\",\"^제[0-9]{1,3}장[가-힣]+\"]},{\"tokenSize\":0,\"prefixes\":[\"^제[0-9]{1,3}절[가-힣]+\"]},{\"tokenSize\":0,\"prefixes\":[\"^제[0-9]{1,3}조(\\\\([가-힣 ]+\\\\))?\"]}]")
    private List<PatternDto> patterns;

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]")
    private List<String> stopPatterns;
}
