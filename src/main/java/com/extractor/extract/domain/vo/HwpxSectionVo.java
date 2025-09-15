package com.extractor.extract.domain.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class HwpxSectionVo {

    private final String id;

    private final String content;

    @Builder
    public HwpxSectionVo(String id, String content) {
        this.id = id;
        this.content = content;
    }
}