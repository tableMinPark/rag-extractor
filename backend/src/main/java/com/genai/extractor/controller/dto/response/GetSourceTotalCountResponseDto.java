package com.genai.extractor.controller.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSourceTotalCountResponseDto {

    private long sourceTotalCount;

    private long passageTotalCount;

    private long chunkTotalCount;
}
