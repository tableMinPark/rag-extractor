package com.document.extractor.adapter.in.dto.response;

import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "문서 변환 에러 여부")
    private Boolean isConvertError;

    @Schema(description = "이전 버전 패시지")
    private List<PassageVo> previousPassages;

    @Schema(description = "현재 버전 패시지")
    private List<PassageVo> currentPassages;

    @Schema(description = "청크")
    private List<ChunkVo> chunks;
}
