package com.document.extractor.application.port;

import com.document.extractor.domain.model.ComnCode;

import java.util.List;

public interface ComnCodePersistencePort {

    /**
     * 그룹 코드 기준 공통 코드 목록 조회
     *
     * @param codeGroup 그룹 코드
     * @return 공통 코드 목록
     */
    List<ComnCode> getComnCodesByCodeGroupPort(String codeGroup);
}
