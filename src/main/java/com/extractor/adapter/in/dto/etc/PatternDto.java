package com.extractor.adapter.in.dto.etc;

import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatternDto {

    @Schema(description = "제약 토큰 수")
    private int tokenSize;

    @Schema(description = "정규식 정보")
    private List<PrefixDto> prefixes;

    public static List<PatternVo> convertPatternVo(List<PatternDto> patternDtos) {
        List<PatternVo> patterns = new ArrayList<>();

        int maxTokenSize = 0;

        for (PatternDto patternDto : patternDtos) {

            List<PrefixVo> prefixes = patternDto.getPrefixes().stream()
                    .map(prefixDto -> PrefixVo.builder()
                            .prefix(prefixDto.getPrefix())
                            .isTitle(prefixDto.getIsTitle())
                            .build())
                    .toList();

            patterns.add(PatternVo.builder()
                    .prefixes(prefixes)
                    .tokenSize(maxTokenSize > patternDto.getTokenSize()
                            ? maxTokenSize
                            : patternDto.getTokenSize())
                    .build());

            maxTokenSize = Math.max(maxTokenSize, patternDto.getTokenSize());
        }

        return patterns;
    }
}
