package com.genai.extractor.vo;

import com.genai.common.repository.entity.CommonCodeEntity;
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

    public static CommonCodeVo of(CommonCodeEntity entity) {
        return CommonCodeVo.builder()
                .code(entity.getCode())
                .codeName(entity.getCodeName())
                .codeGroup(entity.getCodeGroup())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
