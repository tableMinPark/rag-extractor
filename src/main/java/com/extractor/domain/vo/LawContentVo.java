package com.extractor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class LawContentVo {

    private final Long lawContentId;

    private final Long lawId;

    private final Integer version;

    private final String contentType;

    private final String categoryCode;

    private final Integer arrange;

    private final String simpleTitle;

    private final String title;

    private final String content;

    private final List<LawLinkVo> lawLinkVos;
}
