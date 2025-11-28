package com.extractor.adapter.in.dto.request;

import com.extractor.adapter.in.dto.etc.PatternDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkLawsRequestDto {

    @Schema(description = "표 추출 타입", example = "markdown", defaultValue = "markdown")
    private String extractType;

    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"BUCHICK\",\"isTitle\":true },{\"prefix\":\"PYUN\",\"isTitle\":true },{\"prefix\":\"JANG\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JUL\",\"isTitle\":true },{\"prefix\":\"GWAN\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JO\",\"isTitle\":true }]}]", defaultValue = "[]")
    private List<PatternDto> patterns;

    @Schema(description = "제외 패턴", example = "[\"NAME\",\"HISTORY\"]", defaultValue = "[]")
    private List<String> excludeContentTypes;

    @Schema(description = "법령 ID 목록", example = "[27]", defaultValue = "[]")
    private List<Long> lawIds;
}
