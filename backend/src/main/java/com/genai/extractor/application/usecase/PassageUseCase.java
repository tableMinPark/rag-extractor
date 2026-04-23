package com.genai.extractor.application.usecase;

import com.genai.extractor.application.command.GetPassageCommand;
import com.genai.extractor.application.command.GetPassagesCommand;
import com.genai.extractor.application.vo.PassageVo;
import com.genai.global.wrapper.PageWrapper;

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

    /**
     * 총 패시지 수 조회
     *
     * @return 총 패시지 수
     */
    long getPassageTotalCountUseCase();
}
