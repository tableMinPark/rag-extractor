package com.document.extractor.application.usecase;

import com.document.extractor.application.command.CreateSourceCommand;
import com.document.extractor.application.command.GetSourceCommand;
import com.document.extractor.application.command.GetSourcesCommand;
import com.document.extractor.application.vo.ComnCodeVo;
import com.document.extractor.application.vo.SourceVo;
import com.document.extractor.application.wrapper.PageWrapper;

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
    List<ComnCodeVo> getCategoriesSourceUseCase();
}
