package com.document.extractor.adapter.in.dto.response;

import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.vo.SourceVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkResponseDto {

    @Schema(description = "대상 문서")
    private SourceVo source;

    @Schema(description = "패시지")
    private List<PassageVo> passages;

    @Schema(description = "청크")
    private List<ChunkVo> chunks;
}
