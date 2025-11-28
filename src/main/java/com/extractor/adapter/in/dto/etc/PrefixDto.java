package com.extractor.adapter.in.dto.etc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrefixDto {

    @Schema(description = "정규식")
    private String prefix;

    @Schema(description = "삭제 여부")
    private Boolean isTitle;
}
