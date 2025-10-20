package com.extractor.domain.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
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

    @Builder
    public LawLinkVo(Long lawLinkId, Long lawContentId, Long lawId, Integer version, String text, String type, Long targetLawId, String linkCode, String title, String content) {
        this.lawLinkId = lawLinkId;
        this.lawContentId = lawContentId;
        this.lawId = lawId;
        this.version = version;
        this.text = text;
        this.type = type;
        this.targetLawId = targetLawId;
        this.linkCode = linkCode;
        this.title = title;
        this.content = content;
    }
}
