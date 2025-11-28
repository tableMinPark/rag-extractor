package com.extractor.application.usecase;

import com.extractor.application.vo.SourceOptionVo;
import com.extractor.application.vo.SourceVo;

import java.util.List;

public interface SourceUseCase {

    /**
     * 전처리 결과 등록
     *
     * @param sourceOptionVo 결과 등록 옵션
     * @param sourceVos      전처리 결과 목록
     */
    void createSource(SourceOptionVo sourceOptionVo, List<SourceVo> sourceVos);
}
