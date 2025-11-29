package com.extractor.application.command;

import com.extractor.application.vo.ChunkOptionVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ChunkManualCommand {

    private final ChunkOptionVo chunkOption;

    private final Long manualId;
}
