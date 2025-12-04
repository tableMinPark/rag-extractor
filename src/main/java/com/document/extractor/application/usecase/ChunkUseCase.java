package com.document.extractor.application.usecase;

import com.document.extractor.application.command.ChunkBatchCommand;
import com.document.extractor.application.command.ChunkCommand;
import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.vo.ChunkResultVo;

public interface ChunkUseCase {

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     * @return 청킹 결과
     */
    ChunkResultVo chunkFileUseCase(ChunkFileCommand command);

    /**
     * 원격 문서 청킹
     *
     * @param command 원격 문서 청킹 Command
     * @return 청킹 결과
     */
    ChunkResultVo chunkRepoUseCase(ChunkRepoCommand command);

    /**
     * 청킹
     *
     * @param command 청킹 Command
     * @return 청킹 결과
     */
    ChunkResultVo chunkUseCase(ChunkCommand command);

    /**
     * 청킹 배치
     *
     * @param command 청킹 배치 Command
     * @return 청킹 결과
     */
    ChunkResultVo chunkBatchUseCase(ChunkBatchCommand command);
}
