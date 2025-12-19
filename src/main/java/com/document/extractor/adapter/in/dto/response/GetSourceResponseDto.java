package com.document.extractor.adapter.in.dto.response;

import com.document.extractor.application.vo.SourceVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSourceResponseDto {

    private Long sourceId;

    private Long version;

    private String sourceType;

    private String sourceTypeName;

    private String categoryCode;

    private String categoryName;

    private String name;

    private String collectionId;

    private String selectType;

    private String selectTypeName;

    private Boolean isAuto;

    private Boolean isBatch;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifyDt;

    public static GetSourceResponseDto of(SourceVo sourceVo) {
        return GetSourceResponseDto.builder()
                .sourceId(sourceVo.getSourceId())
                .version(sourceVo.getVersion())
                .sourceType(sourceVo.getSourceType())
                .sourceTypeName(sourceVo.getSourceTypeName())
                .categoryCode(sourceVo.getCategoryCode())
                .categoryName(sourceVo.getCategoryName())
                .name(sourceVo.getName())
                .collectionId(sourceVo.getCollectionId())
                .isAuto(sourceVo.getIsAuto())
                .isBatch(sourceVo.getIsBatch())
                .selectType(sourceVo.getSelectType())
                .selectTypeName(sourceVo.getSelectTypeName())
                .sysCreateDt(sourceVo.getSysCreateDt())
                .sysModifyDt(sourceVo.getSysModifyDt())
                .build();
    }

    public static List<GetSourceResponseDto> toList(List<SourceVo> sources) {
        return sources.stream()
                .map(GetSourceResponseDto::of)
                .toList();
    }
}
