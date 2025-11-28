package com.extractor.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class SourceOptionVo {

    private String version;

    private String collectionId;

    private String categoryCode;
}
