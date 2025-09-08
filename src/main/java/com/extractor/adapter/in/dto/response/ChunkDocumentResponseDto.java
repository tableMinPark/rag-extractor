package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.PassageDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkDocumentResponseDto {

    @Schema(description = "패시지 목록")
    private List<PassageDocumentVo> passages;

    @Schema(description = "전처리 패턴 정보")
    private ChunkPatternVo chunkInfo;

    @Builder
    public ChunkDocumentResponseDto(ChunkPatternVo chunkInfo, List<PassageDocumentVo> passages) {
        this.chunkInfo = chunkInfo;
        this.passages = passages;
    }
}
