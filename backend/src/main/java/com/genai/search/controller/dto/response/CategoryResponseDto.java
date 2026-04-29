package com.genai.search.controller.dto.response;

import com.genai.common.vo.CommonCodeVO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryResponseDto {

    private String code;
    private String name;

    public static CategoryResponseDto from(CommonCodeVO vo) {
        return CategoryResponseDto.builder()
                .code(vo.getCode())
                .name(vo.getCodeName())
                .build();
    }

    public static List<CategoryResponseDto> fromList(List<CommonCodeVO> vos) {
        return vos.stream().map(CategoryResponseDto::from).toList();
    }
}
