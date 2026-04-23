package com.genai.extractor.application.port;

import com.genai.extractor.domain.model.CommonCode;

import java.util.List;

public interface CommonCodePersistencePort {

    /**
     * 그룹 코드 기준 공통 코드 목록 조회
     *
     * @param codeGroup 그룹 코드
     * @return 공통 코드 목록
     */
    List<CommonCode> getComnCodesByCodeGroupPort(String codeGroup);
}
