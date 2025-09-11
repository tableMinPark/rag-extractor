package com.extractor.adapter.out;

import com.extractor.adapter.out.entity.OriginalDocumentEntity;
import com.extractor.adapter.out.entity.TrainingDocumentEntity;
import com.extractor.adapter.out.repository.OriginalRepository;
import com.extractor.adapter.out.repository.TrainingRepository;
import com.extractor.application.port.DocumentPersistencePort;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.TrainingDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentPersistencePort {

    private final OriginalRepository originalRepository;

    private final TrainingRepository trainingRepository;

    /**
     * 원본 문서 영속화
     *
     * @param originalDocument 원본 문서
     */
    @Override
    @Transactional
    public OriginalDocument saveOriginalDocumentPort(OriginalDocument originalDocument) {

        OriginalDocumentEntity originalDocumentEntity = originalRepository.save(OriginalDocumentEntity.builder()
                .version(originalDocument.getVersion())
                .docType(originalDocument.getDocType())
                .categoryCode(originalDocument.getCategoryCode())
                .name(originalDocument.getName())
                .filePath(originalDocument.getFilePath())
                .content(originalDocument.getContent())
                .build());

        return OriginalDocument.builder()
                .originalId(originalDocumentEntity.getOriginalId())
                .version(originalDocumentEntity.getVersion())
                .docType(originalDocumentEntity.getDocType())
                .categoryCode(originalDocumentEntity.getCategoryCode())
                .name(originalDocumentEntity.getName())
                .filePath(originalDocumentEntity.getFilePath())
                .content(originalDocument.getContent())
                .build();
    }

    /**
     * 학습 데이터 영속화
     *
     * @param trainingDocument 학습 데이터
     */
    @Override
    @Transactional
    public TrainingDocument saveTrainingDocumentPort(TrainingDocument trainingDocument) {

        TrainingDocumentEntity trainingDocumentEntity = trainingRepository.save(TrainingDocumentEntity.builder()
                .originalId(trainingDocument.getOriginalId())
                .version(trainingDocument.getVersion())
                .docType(trainingDocument.getDocType())
                .categoryCode(trainingDocument.getCategoryCode())
                .title(trainingDocument.getTitle())
                .subTitle(trainingDocument.getSubTitle())
                .thirdTitle(trainingDocument.getThirdTitle())
                .content(trainingDocument.getContent())
                .subContent(trainingDocument.getSubContent())
                .tokenSize(trainingDocument.getTokenSize())
                .build());

        return TrainingDocument.builder()
                .trainingId(trainingDocumentEntity.getTrainingId())
                .originalId(trainingDocumentEntity.getOriginalId())
                .version(trainingDocumentEntity.getVersion())
                .docType(trainingDocumentEntity.getDocType())
                .categoryCode(trainingDocumentEntity.getCategoryCode())
                .title(trainingDocumentEntity.getTitle())
                .subTitle(trainingDocumentEntity.getSubTitle())
                .thirdTitle(trainingDocumentEntity.getThirdTitle())
                .content(trainingDocument.getContent())
                .subContent(trainingDocument.getSubContent())
                .tokenSize(trainingDocument.getTokenSize())
                .build();
    }
}