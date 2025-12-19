package com.document.extractor.application.vo;

import com.document.extractor.domain.model.ComnCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ComnCodeVo {

    private Long codeId;

    private String code;

    private String codeName;

    private String codeGroup;

    private Integer sortOrder;

    public static ComnCodeVo of(ComnCode comnCode) {
        return ComnCodeVo.builder()
                .codeId(comnCode.getCodeId())
                .code(comnCode.getCode())
                .codeName(comnCode.getCodeName())
                .codeGroup(comnCode.getCodeGroup())
                .sortOrder(comnCode.getSortOrder())
                .build();
    }
}
