package com.extractor.domain.model.law;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class LawLink {

    private final Long lawLinkId;

    private final Long lawContentId;

    private final Long lawId;

    private final Integer version;

    private final String content;

    private final String linkTag;

    @Builder
    public LawLink(Long lawLinkId, Long lawContentId, Long lawId, Integer version, String content, String linkTag) {
        this.lawLinkId = lawLinkId;
        this.lawContentId = lawContentId;
        this.lawId = lawId;
        this.version = version;
        this.content = content;
        this.linkTag = linkTag;
    }
}
