package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Passage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PassageVo {

    private Long passageId;

    private Long sourceId;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer tokenSize;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;

    @JsonIgnore
    private final List<ChunkVo> chunkVos;

    public static PassageVo of(Passage passage, List<Chunk> chunks) {
        return PassageVo.builder()
                .passageId(passage.getPassageId())
                .sourceId(passage.getSourceId())
                .title(passage.getTitle())
                .subTitle(passage.getSubTitle())
                .thirdTitle(passage.getThirdTitle())
                .content(passage.getContent())
                .subContent(passage.getSubContent())
                .tokenSize(passage.getTokenSize())
                .sysCreateDt(passage.getSysCreateDt())
                .sysModifyDt(passage.getSysModifyDt())
                .chunkVos(chunks.stream()
                        .map(ChunkVo::of)
                        .toList())
                .build();
    }
}
