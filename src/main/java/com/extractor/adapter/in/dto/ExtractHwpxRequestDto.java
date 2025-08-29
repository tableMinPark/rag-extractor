package com.extractor.adapter.in.dto;

import com.extractor.domain.vo.pattern.PatternVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExtractHwpxRequestDto {

    private List<PatternVo> patterns;

    private List<PatternVo> stopPatterns;

    public ExtractHwpxRequestDto(List<PatternVo> patterns, List<PatternVo> stopPatterns) {
        this.patterns = patterns;
        this.stopPatterns = stopPatterns;
    }
}
