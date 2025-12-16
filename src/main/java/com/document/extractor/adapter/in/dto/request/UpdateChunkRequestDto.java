package com.document.extractor.adapter.in.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateChunkRequestDto {

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;
}
