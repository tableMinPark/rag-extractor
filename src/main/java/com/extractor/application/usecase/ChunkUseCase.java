package com.extractor.application.usecase;

import com.extractor.application.command.ChunkFileCommand;
import com.extractor.application.command.ChunkLawCommand;
import com.extractor.application.command.ChunkManualCommand;
import com.extractor.application.vo.SourceVo;

public interface ChunkUseCase {

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     */
    SourceVo chunkFileUseCase(ChunkFileCommand command);

    /**
     * 법령 문서 청킹
     *
     * @param command 법령 문서 청킹 Command
     */
    SourceVo chunkLawUseCase(ChunkLawCommand command);

    /**
     * 메뉴얼 문서 청킹
     *
     * @param command 메뉴얼 문서 청킹 Command
     */
    SourceVo chunkManualUseCase(ChunkManualCommand command);
}
