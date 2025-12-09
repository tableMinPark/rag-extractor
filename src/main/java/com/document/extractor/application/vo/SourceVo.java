package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Source;
import com.document.extractor.domain.model.SourceStopPattern;
import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.domain.vo.PrefixVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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

    @JsonIgnore
    private String content;

    private String collectionId;

    private Long fileDetailId;

    private final Integer maxTokenSize;

    private final Integer overlapSize;

    private final Boolean isAuto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifyDt;

    private List<PatternVo> patterns;

    private List<String> stopPatterns;

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
                .maxTokenSize(source.getMaxTokenSize())
                .overlapSize(source.getOverlapSize())
                .isAuto(source.getIsAuto())
                .sysCreateDt(source.getSysCreateDt())
                .sysModifyDt(source.getSysModifyDt())
                .patterns(source.getSourcePatterns().stream()
                        .map(sourcePattern -> PatternVo.builder()
                                .tokenSize(sourcePattern.getTokenSize())
                                .prefixes(sourcePattern.getSourcePrefixes().stream()
                                        .map(sourcePrefix -> PrefixVo.builder()
                                                .isTitle(sourcePrefix.getIsTitle())
                                                .prefix(sourcePrefix.getPrefix())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .stopPatterns(source.getSourceStopPatterns().stream()
                        .map(SourceStopPattern::getPrefix)
                        .toList())
                .build();
    }
}
