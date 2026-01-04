package com.document.extractor.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ChunkResultVo {

    private final Boolean isConvertError;

    private final SourceVo source;

    private final List<PassageVo> previousPassages;

    private final List<PassageVo> currentPassages;

    private final List<ChunkVo> chunks;
}
