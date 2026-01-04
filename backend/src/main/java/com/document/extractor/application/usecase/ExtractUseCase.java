package com.document.extractor.application.usecase;

import com.document.extractor.application.command.ExtractFileCommand;
import com.document.extractor.application.command.ExtractFileTextCommand;
import com.document.extractor.application.command.ExtractLawCommand;
import com.document.extractor.application.command.ExtractManualCommand;
import com.document.extractor.application.vo.DocumentVo;
import com.document.extractor.application.vo.ExtractContentVo;

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

    /**
     * 법령 문서 추출
     *
     * @param command 법령 문서 추출 Command
     * @return 법령 문서 추출
     */
    DocumentVo extractLawUseCase(ExtractLawCommand command);

    /**
     * 메뉴얼 문서 추출
     *
     * @param command 메뉴얼 문서 추출 Command
     * @return 메뉴얼 문서 추출
     */
    DocumentVo extractManualUseCase(ExtractManualCommand command);
}