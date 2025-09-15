package com.extractor.chunk.adapter.in.dto.response;

import com.extractor.chunk.application.vo.ChunkVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkLawResponseDto {

    @Schema(description = "청크 수")
    private Integer chunkCount;

    @Schema(description = "청크 목록")
    private List<ChunkVo> chunks;
}
