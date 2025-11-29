package com.extractor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LawLinkVo {

    private final Long lawLinkId;

    private final Long lawContentId;

    private final Long lawId;

    private final Integer version;

    private final String text;

    private final String type;

    private final Long targetLawId;

    private final String linkCode;

    private final String title;

    private final String content;
}
