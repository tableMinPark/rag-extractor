package com.extractor.application.port;

import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.TrainingDocument;

import java.util.List;

public interface DocumentPersistencePort {

    /**
     * 원본 문서 영속화
     * @param originalDocument 원본 문서
     */
    void saveOriginalDocumentPort(OriginalDocument originalDocument);

    /**
     * 학습 데이터 영속화
     * @param trainingDocuments 학습 데이터 목록
     */
    void saveTrainingDocumentsPort(List<TrainingDocument> trainingDocuments);
}
