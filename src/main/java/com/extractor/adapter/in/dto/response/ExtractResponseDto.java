package com.extractor.adapter.in.dto.response;

import com.extractor.application.vo.DocumentLineVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExtractResponseDto {

    @Schema(description = "문서 식별자")
    private String docId;

    @Schema(description = "파일명")
    private String name;

    @Schema(description = "파일 확장자")
    private String extension;

    @Schema(description = "문서 추출 라인")
    private List<DocumentLineVo> lines;

    @Builder
    public ExtractResponseDto(String docId, String name, String extension, List<DocumentLineVo> lines) {
        this.docId = docId;
        this.name = name;
        this.extension = extension;
        this.lines = lines;
    }
}
