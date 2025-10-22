package com.extractor.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "메뉴얼 전처리 분리 정보")
public class ChunkManualRequestDto {

    @Schema(description = "문서 카테고리 코드", example = "TRAIN-MANUAL")
    private String categoryCode;
}
