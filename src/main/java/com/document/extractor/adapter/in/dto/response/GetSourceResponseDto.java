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

    private String categoryCode;

    private String name;

    private String collectionId;

    private Boolean isAuto;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifyDt;

    public static GetSourceResponseDto of(SourceVo sourceVo) {
        return GetSourceResponseDto.builder()
                .sourceId(sourceVo.getSourceId())
                .version(sourceVo.getVersion())
                .sourceType(sourceVo.getSourceType())
                .categoryCode(sourceVo.getCategoryCode())
                .name(sourceVo.getName())
                .collectionId(sourceVo.getCollectionId())
                .isAuto(sourceVo.getIsAuto())
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
