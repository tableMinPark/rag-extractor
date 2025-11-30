package com.document.extractor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PatternVo {

    private final int tokenSize;

    private final List<PrefixVo> prefixes;
}
