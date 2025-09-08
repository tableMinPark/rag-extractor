package com.extractor.adapter.out;

import com.extractor.application.port.DocumentPersistencePort;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.TrainingDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentPersistencePort {

    /**
     * 원본 문서 영속화
     * @param originalDocument 원본 문서
     */
    @Override
    public void saveOriginalDocumentPort(OriginalDocument originalDocument) {

    }

    /**
     * 학습 데이터 영속화
     * @param trainingDocuments 학습 데이터 목록
     */
    @Override
    public void saveTrainingDocumentsPort(List<TrainingDocument> trainingDocuments) {

    }
}
