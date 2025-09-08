package com.extractor.domain.model.law;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class LawContent {

    private final Long lawContentId;

    private final Long lawId;

    private final Integer version;

    private final String contentType;

    private final String categoryCode;

    private final Integer arrange;

    private final String simpleTitle;

    private final String title;

    private final String content;

    @Builder
    public LawContent(Long lawContentId, Long lawId, Integer version, String contentType, String categoryCode, Integer arrange, String simpleTitle, String title, String content) {
        this.lawContentId = lawContentId;
        this.lawId = lawId;
        this.version = version;
        this.contentType = contentType;
        this.categoryCode = categoryCode;
        this.arrange = arrange;
        this.simpleTitle = simpleTitle;
        this.title = title;
        this.content = content;
    }
}
