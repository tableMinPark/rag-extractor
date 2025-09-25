package com.extractor.application.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TrainingDocumentVo {

    @JsonIgnore
    private final Long trainingId;

    @JsonIgnore
    private final Long originalId;

    private final String version;

    private final String docType;

    private final String categoryCode;

    private final String title;

    private final String subTitle;

    private final String thirdTitle;

    private final String content;

    private final String subContent;

    private final Integer tokenSize;

    @Builder
    public TrainingDocumentVo(Long trainingId, Long originalId, String version, String docType, String categoryCode, String title, String subTitle, String thirdTitle, String content, String subContent, Integer tokenSize) {
        this.trainingId = trainingId;
        this.originalId = originalId;
        this.version = version;
        this.docType = docType;
        this.categoryCode = categoryCode;
        this.title = title;
        this.subTitle = subTitle;
        this.thirdTitle = thirdTitle;
        this.content = content;
        this.subContent = subContent;
        this.tokenSize = tokenSize;
    }
}
