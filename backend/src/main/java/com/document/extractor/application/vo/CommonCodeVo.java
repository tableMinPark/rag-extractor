package com.document.extractor.application.vo;

import com.document.extractor.domain.model.CommonCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CommonCodeVo {

    private String codeId;

    private String code;

    private String codeName;

    private String codeGroup;

    private Integer sortOrder;

    public static CommonCodeVo of(CommonCode commonCode) {
        return CommonCodeVo.builder()
                .codeId(commonCode.getCodeId())
                .code(commonCode.getCode())
                .codeName(commonCode.getCodeName())
                .codeGroup(commonCode.getCodeGroup())
                .sortOrder(commonCode.getSortOrder())
                .build();
    }
}
