package com.extractor.domain.processor;

import com.extractor.domain.model.ChunkOld;

import java.util.List;

public interface ChunkProcessor {

    /**
     * 청킹
     */
    List<ChunkOld> chunking();

    /**
     * 청크 본문 저장
     */
    void flushContent();

    /**
     * 본문 생성
     * @return 본문 문자열
     */
    String generateContent();

    /**
     * 부가 본문 생성
     * @return 부가 본문 문자열
     */
    String generateSubContent();
}
