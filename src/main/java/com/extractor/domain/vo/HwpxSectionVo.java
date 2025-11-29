package com.extractor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class HwpxSectionVo {

    private final String id;

    private final String content;
}