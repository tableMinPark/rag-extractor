package com.extractor.application.vo;

import com.extractor.domain.model.Chunk;
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

    public static ChunkVo of(Chunk chunk) {
        return ChunkVo.builder()
                .chunkId(chunk.getChunkId())
                .passageId(chunk.getPassageId())
                .title(chunk.getTitle())
                .subTitle(chunk.getSubTitle())
                .thirdTitle(chunk.getThirdTitle())
                .content(chunk.getContent())
                .subContent(chunk.getSubContent())
                .tokenSize(chunk.getTokenSize())
                .sysCreateDt(chunk.getSysCreateDt())
                .sysModifyDt(chunk.getSysModifyDt())
                .build();
    }
}
