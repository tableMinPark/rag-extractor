package com.document.extractor.application.usecase;

import com.document.extractor.application.command.GetPassageCommand;
import com.document.extractor.application.command.GetPassagesCommand;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.wrapper.PageWrapper;

public interface PassageUseCase {

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
