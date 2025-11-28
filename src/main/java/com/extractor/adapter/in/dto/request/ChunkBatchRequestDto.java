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
public class ChunkBatchRequestDto {

    @Schema(description = "카테고리 코드", example = "TRAIN-ETC", defaultValue = "TRAIN-ETC")
    private String categoryCode;

    @Schema(description = "컬렉션 ID", example = "rag", defaultValue = "rag")
    private String collectionId;

    @Schema(description = "표 추출 타입", example = "markdown", defaultValue = "markdown")
    private String extractType;

    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\",\"isDeleting\":false },{\"prefix\":\"^부(\\\\s+)?칙\",\"isDeleting\":false },{\"prefix\":\"^제[0-9]{1,3}장[가-힣]+\",\"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}절[가-힣]+\",\"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}조(\\\\([가-힣 ]+\\\\))?\",\"isDeleting\":false }]}]", defaultValue = "[]")
    private List<PatternDto> patterns;

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]", defaultValue = "[]")
    private List<String> stopPatterns;

    @Schema(description = "전처리 타입", example = "regex", defaultValue = "token")
    private String selectType;

    @Schema(description = "본문에서 타이틀 추출 여부", example = "true", defaultValue = "true")
    private boolean isExtractTitle;
}
