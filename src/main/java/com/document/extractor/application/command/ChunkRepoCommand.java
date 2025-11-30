package com.document.extractor.application.command;

import com.document.extractor.application.vo.ChunkOptionVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ChunkRepoCommand {

    private final ChunkOptionVo chunkOption;

    private final String repoType;

    private final String repoId;
}
