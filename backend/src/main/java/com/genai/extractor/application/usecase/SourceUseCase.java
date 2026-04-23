package com.genai.extractor.application.usecase;

import com.genai.extractor.application.command.CreateSourceCommand;
import com.genai.extractor.application.command.GetSourceCommand;
import com.genai.extractor.application.command.GetSourcesCommand;
import com.genai.extractor.application.vo.CommonCodeVo;
import com.genai.extractor.application.vo.SourceVo;
import com.genai.global.wrapper.PageWrapper;

import java.util.List;

public interface SourceUseCase {

    /**
     * 파일 대상 문서 등록
     *
     * @param command 파일 대상 문서 등록 Command
     */
    void createSourcesUseCase(CreateSourceCommand command);

    /**
     * 배치 대상 문서 목록 조회
     *
     * @return 배치 대상 문서 목록
     */
    List<SourceVo> getActiveSourcesUseCase();

    /**
     * 대상 문서 조회
     *
     * @param command 대상 문서 조회 Command
     * @return 대상 문서
     */
    SourceVo getSourceUseCase(GetSourceCommand command);

    /**
     * 대상 문서 목록 조회
     *
     * @param command 대상 문서 목록 조회 Command
     * @return 대상 문서 목록
     */
    PageWrapper<SourceVo> getSourcesUseCase(GetSourcesCommand command);

    /**
     * 대상 문서 카테고리 목록 조회
     *
     * @return 대상 문서 카테고리 목록
     */
    List<CommonCodeVo> getCategoriesSourceUseCase();

    /**
     * 총 대상 문서 수 조회
     *
     * @return 총 대상 문서 수
     */
    long getSourceTotalCountUseCase();

    /**
     * 대상 문서 삭제
     *
     * @param sourceId 대상 문서 ID
     */
    void deleteSourceUseCase(Long sourceId);

    /**
     * 대상 문서 배치 여부 수정
     *
     * @param sourceId 대상 문서 ID
     * @param isBatch  배치 여부
     */
    void updateIsBatchUseCase(Long sourceId, boolean isBatch);
}
