package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.TrainingDocumentVo;
import com.extractor.application.vo.ChunkPatternVo;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkDocumentResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "문서 ID")
    private Long originalId;

    @Schema(description = "전처리 이력 버전 코드")
    private String version;

    @Schema(description = "문서명")
    private String name;

    @Schema(description = "문서 분류")
    private String docType;

    @Schema(description = "전처리 분류")
    private String categoryCode;

    @Schema(description = "청크 수")
    private Integer chunkCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "청크 목록")
    private List<TrainingDocumentVo> chunks;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "전처리 패턴 정보")
    private ChunkPatternVo chunkInfo;
}
