package com.document.extractor.application.usecase;

import com.document.extractor.application.command.CreateSourceCommand;

public interface SourceUseCase {

    /**
     * 파일 대상 문서 등록
     *
     * @param command 파일 대상 문서 등록 Command
     */
    void createSourcesUseCase(CreateSourceCommand command);
}
