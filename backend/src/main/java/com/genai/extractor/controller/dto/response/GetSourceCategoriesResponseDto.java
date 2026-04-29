package com.genai.extractor.controller.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSourceCategoriesResponseDto {

    private String code;

    private String name;
}
