package com.extractor.adapter.in.dto.request;

import com.extractor.adapter.in.dto.etc.PatternDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "법령 전처리 분리 정보")
public class ChunkLawRequestDto {

    @Schema(description = "법령 ID", example = "319")
    private Long lawId;

    @Schema(description = "전처리 패턴", example = "[{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"addenda\", \"isDeleting\":false },{\"prefix\":\"part\", \"isDeleting\":false },{\"prefix\":\"chapter\", \"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"section\", \"isDeleting\":false },{\"prefix\":\"subsection\",\t \"isDeleting\":false }]},{\"tokenSize\":0,\"prefixes\":[{\"prefix\":\"article\", \"isDeleting\":false }]}]")
    private List<PatternDto> patterns;

    @Schema(description = "제외 패턴", example = "[\"lawname\",\"history\"]")
    private List<String> excludeContentTypes;
}
