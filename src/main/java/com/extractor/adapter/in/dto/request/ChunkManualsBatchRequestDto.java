package com.extractor.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkManualsBatchRequestDto {

    @Schema(description = "카테고리 코드", example = "TRAIN-MANUAL", defaultValue = "TRAIN_MANUAL")
    private String categoryCode;

    @Schema(description = "컬렉션 ID", example = "rag", defaultValue = "rag")
    private String collectionId;

    @Schema(description = "표 추출 타입", example = "markdown", defaultValue = "markdown")
    private String extractType;

    @Schema(description = "최대 토큰 수", example = "-1", defaultValue = "-1")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "메뉴얼 ID 목록", example = "[ 40662 ]", defaultValue = "[]")
    private List<Long> manualIds;
}
