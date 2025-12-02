package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Source;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class SourceVo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long sourceId;

    private Long version;

    private String sourceType;

    private String categoryCode;

    private String name;

    private String content;

    private String collectionId;

    private Long fileDetailId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime sysCreateDt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
