package com.extractor.domain.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
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

    @Builder
    public LawContentVo(Long lawContentId, Long lawId, Integer version, String contentType, String categoryCode, Integer arrange, String simpleTitle, String title, String content, List<LawLinkVo> lawLinkVos) {
        this.lawContentId = lawContentId;
        this.lawId = lawId;
        this.version = version;
        this.contentType = contentType;
        this.categoryCode = categoryCode;
        this.arrange = arrange;
        this.simpleTitle = simpleTitle;
        this.title = title;
        this.content = content;
        this.lawLinkVos = lawLinkVos;
    }
}
