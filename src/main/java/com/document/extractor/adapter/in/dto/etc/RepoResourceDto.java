package com.document.extractor.adapter.in.dto.etc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepoResourceDto {

    @NotBlank
    @Schema(description = "대상 문서명", example = "테스트 문서", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originFileName;

    @NotBlank
    @Schema(description = "리소스 식별자", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank
    @Pattern(regexp = "json", message = "json 만 지원")
    @Schema(description = "리소스 응답 컨텐츠 타입", example = "json", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ext;

    @NotBlank
    @Pattern(regexp = "^(?!/)(?!.*/$).+", message = "경로는 '/'로 시작하거나 끝날 수 없음")
    @Schema(description = "리소스 경로", example = "document/data", requiredMode = Schema.RequiredMode.REQUIRED)
    private String path;

    @NotBlank
    @Pattern(regexp = "^(?!/)(?!.*/$).+", message = "경로는 '/'로 시작하거나 끝날 수 없음")
    @Schema(description = "리소스 식별자", example = "dataId?type=markdown", requiredMode = Schema.RequiredMode.REQUIRED)
    private String urn;
}
