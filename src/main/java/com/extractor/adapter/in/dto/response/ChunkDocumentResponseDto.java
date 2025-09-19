package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.TrainingDocumentVo;
import com.extractor.domain.vo.ChunkPatternVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkDocumentResponseDto {

    @Schema(description = "청크 수")
    private Integer chunkCount;

    @Schema(description = "청크 목록")
    private List<TrainingDocumentVo> chunks;

    @Schema(description = "전처리 패턴 정보")
    private ChunkPatternVo chunkInfo;
}
