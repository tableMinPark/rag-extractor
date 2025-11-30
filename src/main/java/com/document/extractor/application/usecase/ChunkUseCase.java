package com.document.extractor.application.usecase;

import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.vo.SourceVo;

public interface ChunkUseCase {

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     */
    SourceVo chunkFileUseCase(ChunkFileCommand command);

    /**
     * 원격 문서 청킹
     *
     * @param command 원격 문서 청킹 Command
     */
    SourceVo chunkRepoUseCase(ChunkRepoCommand command);
}
