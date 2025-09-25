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
@Schema(description = "전처리 패턴 정보")
public class ChunkDocumentRequestDto {

    @Schema(description = "최대 토큰 수", example = "1000")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "200")
    private Integer overlapSize;

    @Schema(description = "문서 카테고리 코드", example = "TRAIN-DOC")
    private String categoryCode;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\",\"isDeleting\":false },{\"prefix\":\"^부(\\\\s+)?칙\",\"isDeleting\":false },{\"prefix\":\"^제[0-9]{1,3}장[가-힣]+\",\"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}절[가-힣]+\",\"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}조(\\\\([가-힣 ]+\\\\))?\",\"isDeleting\":false }]}]")
    private List<PatternDto> patterns;

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]")
    private List<String> stopPatterns;
}
