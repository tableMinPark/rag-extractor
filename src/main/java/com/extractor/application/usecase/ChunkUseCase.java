package com.extractor.application.usecase;

import com.extractor.application.vo.ChunkDocumentVo;
import com.extractor.application.vo.ChunkPatternVo;
import com.extractor.application.vo.FileDocumentVo;
import com.extractor.global.enums.ChunkType;
import com.extractor.global.enums.ExtractType;

public interface ChunkUseCase {

    /**
     * 한글 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     * @param extractType    표 데이터 변환 타입
     */
    ChunkDocumentVo chunkHwpxDocumentUseCase(String version, String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo, ExtractType extractType, ChunkType chunkType);

    /**
     * PDF 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    ChunkDocumentVo chunkPdfDocumentUseCase(String version, String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo, ChunkType chunkType);

    /**
     * 법령 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param lawId          법령 ID
     * @param chunkPatternVo 청킹 패턴 정보
     */
    ChunkDocumentVo chunkLawDocumentUseCase(String version, String categoryCode, Long lawId, ChunkPatternVo chunkPatternVo);

    /**
     * 메뉴얼 문서 청킹
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param manualId       메뉴얼 ID
     */
    ChunkDocumentVo chunkManualDocumentUseCase(String version, String categoryCode, Long manualId);
}
