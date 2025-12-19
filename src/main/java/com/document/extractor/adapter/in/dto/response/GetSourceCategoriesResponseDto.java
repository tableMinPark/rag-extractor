package com.document.extractor.adapter.in.dto.response;

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
