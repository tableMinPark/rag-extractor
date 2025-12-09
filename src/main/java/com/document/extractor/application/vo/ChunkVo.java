package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Chunk;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class ChunkVo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long chunkId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long passageId;

    private Long version;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String compactContent;

    private String subContent;

    private Integer contentTokenSize;

    private Integer compactContentTokenSize;

    private Integer subContentTokenSize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifyDt;

    public static ChunkVo of(Chunk chunk) {
        return ChunkVo.builder()
                .chunkId(chunk.getChunkId())
                .passageId(chunk.getPassageId())
                .version(chunk.getVersion())
                .title(chunk.getTitle())
                .subTitle(chunk.getSubTitle())
                .thirdTitle(chunk.getThirdTitle())
                .content(chunk.getContent())
                .compactContent(chunk.getCompactContent())
                .subContent(chunk.getSubContent())
                .contentTokenSize(chunk.getContent().length())
                .compactContentTokenSize(chunk.getCompactContent().length())
                .subContentTokenSize(chunk.getSubContent().length())
                .sysCreateDt(chunk.getSysCreateDt())
                .sysModifyDt(chunk.getSysModifyDt())
                .build();
    }
}
