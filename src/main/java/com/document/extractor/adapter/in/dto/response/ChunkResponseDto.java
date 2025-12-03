package com.document.extractor.adapter.in.dto.response;

import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.vo.SourceVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkResponseDto {

    private String fileName;

    private Long version;

    @JsonIgnore
    @Schema(description = "대상 문서")
    private SourceVo source;

    @JsonIgnore
    @Schema(description = "패시지")
    private List<PassageVo> passages;

    @Schema(description = "청크")
    private List<ChunkVo> chunks;

    @Builder
    public ChunkResponseDto(SourceVo source, List<PassageVo> passages, List<ChunkVo> chunks) {
        this.fileName = source.getName();
        this.version = source.getVersion();
        this.source = source;
        this.passages = passages;
        this.chunks = chunks;
    }
}
