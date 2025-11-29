package com.extractor.adapter.in.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkManualsRequestDto {

    @Pattern(regexp = "html|markdown", message = "markdown 과 html 만 지원")
    @Schema(description = "표 추출 타입", example = "markdown", defaultValue = "markdown")
    private String extractType;

    @Schema(description = "최대 토큰 수", example = "-1", defaultValue = "-1")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "메뉴얼 ID 목록", example = "[ 40662 ]", defaultValue = "[]")
    private List<Long> manualIds;
}
