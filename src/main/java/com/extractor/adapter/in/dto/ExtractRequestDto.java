package com.extractor.adapter.in.dto;

import com.extractor.domain.vo.pattern.PatternVo;
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

    @Schema(description = "기본 제약 토큰 수", example = "1000")
    private Integer tokenSize;

    @Schema(description = "전처리 패턴", example = "[{\"depth\":0,\"tokenSize\":1000,\"prefix\":\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\"},{\"depth\":0,\"tokenSize\":1000,\"prefix\":\"^부(\\\\s+)?칙\"},{\"depth\":0,\"tokenSize\":1000,\"prefix\":\"^제[0-9]{1,3}장[가-힣]+\"},{\"depth\":1,\"tokenSize\":1000,\"prefix\":\"^제[0-9]{1,3}절[가-힣]+\"},{\"depth\":2,\"tokenSize\":1000,\"prefix\":\"^제[0-9]{1,3}조(\\\\([가-힣]+\\\\))?\"}]")
    private List<PatternVo> patterns;

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]")
    private List<String> stopPatterns;
}
