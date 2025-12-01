package com.document.extractor.application.usecase;

import com.document.extractor.application.command.ChunkBatchCommand;
import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.vo.ChunkResultVo;
import com.document.extractor.application.vo.SourceVo;

import java.util.List;

public interface ChunkUseCase {

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     */
    ChunkResultVo chunkFileUseCase(ChunkFileCommand command);

    /**
     * 원격 문서 청킹
     *
     * @param command 원격 문서 청킹 Command
     */
    ChunkResultVo chunkRepoUseCase(ChunkRepoCommand command);

    /**
     * 청킹 배치
     *
     * @param command 청킹 배치 Command
     */
    ChunkResultVo chunkBatchUseCase(ChunkBatchCommand command);

    /**
     * 배치 대상 문서 목록 조회
     *
     * @return 배치 대상 문서 목록
     */
    List<SourceVo> getActiveSourcesUseCase();
}
