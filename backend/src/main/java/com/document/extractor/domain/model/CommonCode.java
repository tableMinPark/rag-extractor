package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CommonCode {

    private String codeId;

    private String code;

    private String codeName;

    private String codeGroup;

    private Integer sortOrder;
}
