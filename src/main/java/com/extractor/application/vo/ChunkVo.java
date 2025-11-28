package com.extractor.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class ChunkVo {

    private Long chunkId;

    private Long passageId;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer tokenSize;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;
}
