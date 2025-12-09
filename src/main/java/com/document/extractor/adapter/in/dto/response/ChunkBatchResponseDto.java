package com.document.extractor.adapter.in.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkBatchResponseDto {

    @Schema(description = "문서 변환 에러 여부")
    private Boolean isConvertError;

    @Schema(description = "파일명")
    private String fileName;

    @Schema(description = "버전 코드")
    private Long version;

    @Schema(description = "총 패시지 수")
    private Integer totalPassageCount;

    @Schema(description = "총 청크 수")
    private Integer totalChunkCount;
}
