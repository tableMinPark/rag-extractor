package com.extractor.application.usecase;

import com.extractor.application.vo.ChunkOptionVo;
import com.extractor.application.vo.FileVo;
import com.extractor.application.vo.SourceVo;

public interface ChunkUseCase {

    /**
     * 파일 청킹
     *
     * @param chunkOptionVo 청킹 옵션
     * @param fileVo        파일
     */
    SourceVo chunkFileUseCase(ChunkOptionVo chunkOptionVo, FileVo fileVo);

    /**
     * 법령 문서 청킹
     *
     * @param chunkOptionVo 청킹 패턴 정보
     * @param lawId         법령 ID
     */
    SourceVo chunkLawUseCase(ChunkOptionVo chunkOptionVo, Long lawId);

    /**
     * 메뉴얼 문서 청킹
     *
     * @param chunkOptionVo 청킹 패턴 정보
     * @param manualId      메뉴얼 ID
     */
    SourceVo chunkManualUseCase(ChunkOptionVo chunkOptionVo, Long manualId);
}
