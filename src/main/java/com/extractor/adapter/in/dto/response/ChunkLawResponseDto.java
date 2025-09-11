package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.PassageVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkLawResponseDto {

    @Schema(description = "패시지 수")
    private Integer passageCount;

    @Schema(description = "패시지 목록")
    private List<PassageVo> passages;
}
