package com.extractor.application.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class SourceVo {

    private Long sourceId;

    private String version;

    private String sourceType;

    private String categoryCode;

    private String name;

    private String content;

    private String collectionId;

    private Long fileDetailId;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;

    @JsonIgnore
    private final List<PassageVo> passageVos;
}
