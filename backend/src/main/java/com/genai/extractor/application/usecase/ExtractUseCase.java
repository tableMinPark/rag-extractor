package com.genai.extractor.application.usecase;

import com.genai.extractor.application.command.ExtractFileCommand;
import com.genai.extractor.application.command.ExtractFileTextCommand;
import com.genai.extractor.application.vo.ExtractContentVo;

import java.util.List;

public interface ExtractUseCase {

    /**
     * 문서 추출
     *
     * @param command 문서 추출 Command
     * @return 추출 내용 목록
     */
    List<ExtractContentVo> extractFileUseCase(ExtractFileCommand command);

    /**
     * 문서 텍스트 추출
     *
     * @param command 문서 텍스트 추출 Command
     * @return 추출 텍스트
     */
    String extractFileTextUseCase(ExtractFileTextCommand command);
}