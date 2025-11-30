package com.document.extractor.adapter.in.dto.request;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkReposRequestDto {

    @NotBlank
    @Pattern(regexp = "html|markdown", message = "markdown 과 html 만 지원")
    @Schema(description = "표 추출 타입", example = "markdown", defaultValue = "markdown")
    private String extractType;

    @NotNull
    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200")
    private Integer maxTokenSize;

    @NotNull
    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"BUCHICK\",\"isTitle\":true },{\"prefix\":\"PYUN\",\"isTitle\":true },{\"prefix\":\"JANG\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JUL\",\"isTitle\":true },{\"prefix\":\"GWAN\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JO\",\"isTitle\":true }]}]", defaultValue = "[]")
    private List<PatternDto> patterns = new ArrayList<>();

    @Schema(description = "제외 패턴", example = "[\"NAME\",\"HISTORY\"]", defaultValue = "[]")
    private List<String> excludeContentTypes = new ArrayList<>();

    @NotBlank
    @Pattern(regexp = "law|manual", message = "law 과 manual 만 지원")
    @Schema(description = "원격 문서 타입", example = "manual", defaultValue = "manual")
    private String repoType;

    @NotNull
    @Schema(description = "원격 문서 ID 목록", example = "[27]", defaultValue = "[]")
    private List<String> repoIds;
}
