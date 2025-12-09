package com.document.extractor.adapter.in.dto.response;

import com.document.extractor.application.vo.ChunkVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetChunkResponseDto {

    private long chunkId;

    private long passageId;

    private long version;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String compactContent;

    private String subContent;

    private int contentTokenSize;

    private int compactContentTokenSize;

    private int subContentTokenSize;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifyDt;

    public static GetChunkResponseDto of(ChunkVo chunkVo) {
        return GetChunkResponseDto.builder()
                .chunkId(chunkVo.getChunkId())
                .passageId(chunkVo.getPassageId())
                .version(chunkVo.getVersion())
                .title(chunkVo.getTitle())
                .subTitle(chunkVo.getSubTitle())
                .thirdTitle(chunkVo.getThirdTitle())
                .content(chunkVo.getContent())
                .compactContent(chunkVo.getCompactContent())
                .subContent(chunkVo.getSubContent())
                .contentTokenSize(chunkVo.getContentTokenSize())
                .compactContentTokenSize(chunkVo.getCompactContentTokenSize())
                .subContentTokenSize(chunkVo.getSubContentTokenSize())
                .sysCreateDt(chunkVo.getSysCreateDt())
                .sysModifyDt(chunkVo.getSysModifyDt())
                .build();
    }

    public static List<GetChunkResponseDto> toList(List<ChunkVo> chunkVos) {
        return chunkVos.stream()
                .map(GetChunkResponseDto::of)
                .toList();
    }
}
