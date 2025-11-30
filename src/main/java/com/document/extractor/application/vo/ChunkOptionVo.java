package com.document.extractor.application.vo;

import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.application.enums.SelectType;
import com.document.global.enums.ExtractType;
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
}
