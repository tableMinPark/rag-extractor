package com.document.extractor.adapter.in.dto.etc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepoResourceDto {

    @Schema(description = "대상 문서명", example = "테스트 문서")
    private String originFileName;

    @Schema(description = "리소스 식별자", example = "1")
    private String fileName;

    @Pattern(regexp = "json", message = "json 만 지원")
    @Schema(description = "리소스 응답 컨텐츠 타입", example = "json")
    private String ext;

    @Pattern(regexp = "^(?!/)(?!.*/$).+", message = "경로는 '/'로 시작하거나 끝날 수 없음")
    @Schema(description = "리소스 경로", example = "document/data")
    private String path;

    @Pattern(regexp = "^(?!/)(?!.*/$).+", message = "경로는 '/'로 시작하거나 끝날 수 없음")
    @Schema(description = "리소스 식별자", example = "dataId?type=markdown")
    private String urn;
}
