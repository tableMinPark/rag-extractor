package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Source;
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

    private Long version;

    private String sourceType;

    private String categoryCode;

    private String name;

    private String content;

    private String collectionId;

    private Long fileDetailId;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;

    public static SourceVo of(Source source) {
        return SourceVo.builder()
                .sourceId(source.getSourceId())
                .version(source.getVersion())
                .sourceType(source.getSourceType().getCode())
                .categoryCode(source.getCategoryCode())
                .name(source.getName())
                .content(source.getContent())
                .collectionId(source.getCollectionId())
                .fileDetailId(source.getFileDetailId())
                .sysCreateDt(source.getSysCreateDt())
                .sysModifyDt(source.getSysModifyDt())
                .build();
    }
}
