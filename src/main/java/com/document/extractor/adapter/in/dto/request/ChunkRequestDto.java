package com.document.extractor.adapter.in.dto.request;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkRequestDto {

    @Pattern(regexp = "html|markdown", message = "markdown 과 html 만 지원")
    @Schema(description = "표 추출 타입", example = "html", defaultValue = "html")
    private String extractType;

    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\",\"isTitle\":true },{\"prefix\":\"^부(\\\\s+)?칙\",\"isTitle\":true },{\"prefix\":\"^제[0-9]{1,3}장[가-힣]+\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}절[가-힣]+\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}조(\\\\([가-힣 ]+\\\\))?\",\"isTitle\":true }]}]", defaultValue = "[]")
    private List<PatternDto> patterns;

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]", defaultValue = "[]")
    private List<String> stopPatterns;

    @Schema(description = "전처리 타입", example = "regex", defaultValue = "token")
    private String selectType;
}
