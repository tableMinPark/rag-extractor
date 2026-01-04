package com.document.extractor.adapter.in.dto.request;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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

    @Pattern(regexp = "html|markdown", message = "markdown 과 html 만 지원")
    @Schema(description = "표 추출 타입", example = "html", defaultValue = "html", requiredMode =  Schema.RequiredMode.NOT_REQUIRED)
    private String extractType = "html";

    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer maxTokenSize = 1200;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer overlapSize = 0;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"BUCHICK\",\"isTitle\":true },{\"prefix\":\"PYUN\",\"isTitle\":true },{\"prefix\":\"JANG\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JUL\",\"isTitle\":true },{\"prefix\":\"GWAN\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"JO\",\"isTitle\":true }]}]", defaultValue = "[]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<PatternDto> patterns = new ArrayList<>();

    @Schema(description = "제외 패턴", example = "[\"NAME\",\"HISTORY\"]", defaultValue = "[]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> stopPatterns = new ArrayList<>();

    @NotEmpty
    @Schema(description = "원격 문서 URI", example = "localhost:8001/document", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> uris;

    @Pattern(regexp = "regex|token|none", message = "regex 과 token 만 지원")
    @Schema(description = "전처리 타입", example = "regex", defaultValue = "none", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String selectType = "none";
}
