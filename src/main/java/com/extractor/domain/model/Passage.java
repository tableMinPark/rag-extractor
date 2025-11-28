package com.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class Passage {

    private Long passageId;

    private Long sourceId;

    @Setter
    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer tokenSize;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;
}
