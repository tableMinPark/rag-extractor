package com.genai.extractor.application.vo;

import com.genai.extractor.domain.model.CommonCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CommonCodeVo {

    private String code;

    private String codeName;

    private String codeGroup;

    private Integer sortOrder;

    public static CommonCodeVo of(CommonCode commonCode) {
        return CommonCodeVo.builder()
                .code(commonCode.getCode())
                .codeName(commonCode.getCodeName())
                .codeGroup(commonCode.getCodeGroup())
                .sortOrder(commonCode.getSortOrder())
                .build();
    }
}
