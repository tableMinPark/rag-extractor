package com.document.extractor.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ChunkResultVo {

    private final SourceVo source;

    private final List<PassageVo> passages;

    private final List<ChunkVo> chunks;
}
