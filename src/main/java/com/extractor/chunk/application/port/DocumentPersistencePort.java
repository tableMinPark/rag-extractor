package com.extractor.chunk.application.port;

import com.extractor.chunk.domain.model.OriginalDocument;
import com.extractor.chunk.domain.model.TrainingDocument;

public interface DocumentPersistencePort {

    /**
     * 원본 문서 영속화
     *
     * @param originalDocument 원본 문서
     */
    OriginalDocument saveOriginalDocumentPort(OriginalDocument originalDocument);

    /**
     * 학습 데이터 영속화
     *
     * @param trainingDocument 학습 데이터
     */
    TrainingDocument saveTrainingDocumentPort(TrainingDocument trainingDocument);
}
