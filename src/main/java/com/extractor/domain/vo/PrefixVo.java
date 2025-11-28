package com.extractor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PrefixVo {

    private final String prefix;

    private final Boolean isTitle;
}