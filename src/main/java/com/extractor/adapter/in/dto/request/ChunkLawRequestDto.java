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
@Schema(description = "법령 전처리 분리 정보")
public class ChunkLawRequestDto {

    @Schema(description = "표 추출 타입", example = "markdown")
    private String extractType;

    @Schema(description = "최대 토큰 수", example = "1000")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "200")
    private Integer overlapSize;

    @Schema(description = "문서 카테고리 코드", example = "TRAIN-LAW")
    private String categoryCode;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"BUCHICK\",\"isDeleting\":false },{\"prefix\":\"PYUN\",\"isDeleting\":false },{\"prefix\":\"JANG\",\"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JUL\",\"isDeleting\":false },{\"prefix\":\"GWAN\",\"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JO\",\"isDeleting\":false }]}]")
    private List<PatternDto> patterns;

    @Schema(description = "제외 패턴", example = "[\"NAME\",\"HISTORY\"]")
    private List<String> excludeContentTypes;
}
