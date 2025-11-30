package com.document.extractor.application.command;

import com.document.extractor.application.vo.ChunkOptionVo;
import com.document.extractor.application.vo.FileVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ChunkFileCommand {

    private final ChunkOptionVo chunkOption;

    private final FileVo file;
}
