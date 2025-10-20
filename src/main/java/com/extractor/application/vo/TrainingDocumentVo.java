package com.extractor.application.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TrainingDocumentVo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long trainingId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long originalId;

    private final String version;

    private final String docType;

    private final String categoryCode;

    private final String title;

    private final String subTitle;

    private final String thirdTitle;

    private final String content;

    private final String subContent;

    private final Integer totalTokenSize;

    private final Integer contentTokenSize;

    private final Integer subContentTokenSize;

    @Builder
    public TrainingDocumentVo(Long trainingId, Long originalId, String version, String docType, String categoryCode, String title, String subTitle, String thirdTitle, String content, String subContent, Integer totalTokenSize, Integer contentTokenSize, Integer subContentTokenSize) {
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
        this.totalTokenSize = totalTokenSize;
        this.contentTokenSize = contentTokenSize;
        this.subContentTokenSize = subContentTokenSize;
    }
}
