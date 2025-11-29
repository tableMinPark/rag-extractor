package com.extractor.application.command;

import com.extractor.application.vo.ChunkOptionVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ChunkLawCommand {

    private final ChunkOptionVo chunkOption;

    private final Long lawId;
}
