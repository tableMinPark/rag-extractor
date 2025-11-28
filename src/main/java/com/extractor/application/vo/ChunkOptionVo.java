package com.extractor.application.vo;

import com.extractor.domain.vo.PatternVo;
import com.extractor.global.enums.SelectType;
import com.extractor.global.enums.ExtractType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ChunkOptionVo {

    private final ExtractType extractType;

    private final SelectType selectType;

    private final List<PatternVo> patterns;

    private final List<String> antiPatterns;

    private final int maxTokenSize;

    private final int overlapSize;

    private final boolean isExtractTitle;
}
