package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.PassageVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkDocumentResponseDto {

    @Schema(description = "패시지 목록")
    private List<PassageVo> passages;

    @Schema(description = "전처리 패턴 정보")
    private ChunkPatternVo chunkInfo;
}
