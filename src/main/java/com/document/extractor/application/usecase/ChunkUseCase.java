package com.document.extractor.application.usecase;

import com.document.extractor.application.command.*;
import com.document.extractor.application.vo.ChunkResultVo;
import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.wrapper.PageWrapper;

import java.util.List;

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
    ChunkResultVo chunkSourceUseCase(ChunkSourceCommand command);

    /**
     * 청킹 배치
     *
     * @param command 청킹 배치 Command
     * @return 청킹 결과
     */
    ChunkResultVo chunkBatchUseCase(ChunkBatchCommand command);

    /**
     * 청크 등록
     *
     * @param command 청크 등록 Command
     */
    void createChunkUseCase(CreateChunkCommand command);

    /**
     * 청크 조회
     *
     * @param command 청크 조회 Command
     * @return 청크
     */
    ChunkVo getChunkUseCase(GetChunkCommand command);

    /**
     * 청크 목록 조회
     *
     * @param command 청크 목록 조회 Command
     * @return 청크 목록
     */
    PageWrapper<ChunkVo> getChunksUseCase(GetChunksCommand command);

    /**
     * 청크 수정
     *
     * @param command 청크 수정 Command
     */
    void updateChunkUseCase(UpdateChunkCommand command);

    /**
     * 청크 삭제
     *
     * @param command 청크 삭제 Command
     */
    void deleteChunkUseCase(DeleteChunkCommand command);

    /**
     * 패시지 조회
     *
     * @param command 패시지 조회 Command
     * @return 패시지
     */
    PassageVo getPassageUseCase(GetPassageCommand command);

    /**
     * 패시지 목록 조회
     *
     * @param command 패시지 목록 조회 Command
     * @return 패시지 목록
     */
    PageWrapper<PassageVo> getPassagesUseCase(GetPassagesCommand command);

}
