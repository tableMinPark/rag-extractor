package com.document.extractor.adapter.in.dto.request;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import com.document.extractor.adapter.in.dto.etc.RepoResourceDto;
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
public class CreateRepoSourceRequestDto {

    @NotBlank
    @Schema(description = "대상 문서 분류", example = "TRAIN-EXAMPLE", defaultValue = "TRAIN-EXAMPLE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryCode;

    @NotBlank
    @Schema(description = "컬렉션 ID", example = "COLLECTION-ID", defaultValue = "COLLECTION-ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String collectionId;

    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer maxTokenSize = 1200;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer overlapSize = 0;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\",\"isTitle\":true },{\"prefix\":\"^부(\\\\s+)?칙\",\"isTitle\":true },{\"prefix\":\"^제[0-9]{1,3}장[가-힣]+\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}절[가-힣]+\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}조(\\\\([가-힣 ]+\\\\))?\",\"isTitle\":true }]}]", defaultValue = "[]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<PatternDto> patterns = new ArrayList<>();

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]", defaultValue = "[]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> stopPatterns = new ArrayList<>();

    @Pattern(regexp = "regex|token|none", message = "regex 과 token 만 지원")
    @Schema(description = "전처리 타입", example = "regex", defaultValue = "token", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String selectType = "none";

    @NotBlank
    @Schema(description = "리소스 HOST", example = "localhost", requiredMode = Schema.RequiredMode.REQUIRED)
    private String host;

    @NotNull
    @Schema(description = "리소스 PORT", example = "8080", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer port;

    @NotNull
    @Schema(description = "리소스 정보", example = "[{\"originFileName\":\"테스트 문서\",\"remoteId\":\"1\"}]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RepoResourceDto> repoResources;

    @Schema(description = "자동화 처리 여부 ", example = "false", defaultValue = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isAuto;
}
