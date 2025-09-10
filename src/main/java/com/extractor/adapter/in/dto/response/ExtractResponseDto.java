package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.ExtractContentVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResponseDto {

    @Schema(description = "파일명")
    private String name;

    @Schema(description = "파일 확장자")
    private String extension;

    @Schema(description = "문서 추출 라인")
    private List<ExtractContentVo> lines;
}
