package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.PassageDocumentVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkLawResponseDto {

    @Schema(description = "패시지 목록")
    private List<PassageDocumentVo> passages;

    @Builder
    public ChunkLawResponseDto(List<PassageDocumentVo> passages) {
        this.passages = passages;
    }
}
