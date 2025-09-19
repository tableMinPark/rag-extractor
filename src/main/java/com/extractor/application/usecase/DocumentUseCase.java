package com.extractor.application.usecase;

import com.extractor.application.vo.OriginalDocumentVo;
import com.extractor.application.vo.TrainingDocumentVo;

import java.util.List;

public interface DocumentUseCase {

    /**
     * 전처리 문서 등록
     *
     * @param originalDocumentVo  원본 문서
     * @param trainingDocumentVos 전처리 문서 목록
     */
    void registerDocument(OriginalDocumentVo originalDocumentVo, List<TrainingDocumentVo> trainingDocumentVos);
}
