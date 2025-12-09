package com.document.extractor.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkSourcesRequestDto {

    @NotNull
    @Schema(description = "대상 문서 ID 목록", example = "[1]", defaultValue = "[]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> sourceIds;
}
