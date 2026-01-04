package com.document.extractor.adapter.in.dto.response;

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
