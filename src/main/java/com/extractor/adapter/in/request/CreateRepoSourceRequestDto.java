package com.extractor.adapter.in.request;

import com.extractor.adapter.in.dto.PatternDto;
import com.extractor.adapter.in.dto.RepoResourceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateRepoSourceRequestDto {

    @Schema(description = "대상 문서 분류", example = "TRAIN-EXAMPLE", defaultValue = "TRAIN-EXAMPLE")
    private String categoryCode;

    @Schema(description = "컬렉션 ID", example = "COLLECTION-ID", defaultValue = "COLLECTION-ID")
    private String collectionId;

    @Schema(description = "최대 토큰 수", example = "1200", defaultValue = "1200")
    private Integer maxTokenSize;

    @Schema(description = "토큰 초과 시, 오버랩 사이즈", example = "0", defaultValue = "0")
    private Integer overlapSize;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^[\\\\[\\\\(][^\\\\]\\\\)]*별표[^\\\\]\\\\)]*[\\\\]\\\\)]\",\"isTitle\":true },{\"prefix\":\"^부(\\\\s+)?칙\",\"isTitle\":true },{\"prefix\":\"^제[0-9]{1,3}장[가-힣]+\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}절[가-힣]+\",\"isTitle\":true }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"^제[0-9]{1,3}조(\\\\([가-힣 ]+\\\\))?\",\"isTitle\":true }]}]", defaultValue = "[]")
    private List<PatternDto> patterns;

    @Schema(description = "전처리 중단 패턴", example = "[\"^[\\\\[\\\\(][^\\\\]\\\\)]*(별지|별첨|서식)[^\\\\]\\\\)]*[\\\\]\\\\)]\"]", defaultValue = "[]")
    private List<String> stopPatterns;

    @Schema(description = "리소스 HOST", example = "localhost")
    private String host;

    @Schema(description = "리소스 PORT", example = "8080")
    private Integer port;

    @Pattern(regexp = "^(?!/)(?!.*/$).+", message = "경로는 '/'로 시작하거나 끝날 수 없음")
    @Schema(description = "리소스 경로", example = "data")
    private String path;

    @Schema(description = "리소스 정보", example = "[{\"originFileName\":\"테스트 문서\",\"remoteId\":\"1\"}]")
    private List<RepoResourceDto> remoteResources;
}
