package com.extractor.application.service;

import com.extractor.application.port.DocumentPersistencePort;
import com.extractor.application.usecase.DocumentUseCase;
import com.extractor.application.vo.OriginalDocumentVo;
import com.extractor.application.vo.TrainingDocumentVo;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.TrainingDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService implements DocumentUseCase {

    private final DocumentPersistencePort documentPersistencePort;

    /**
     * 전처리 문서 등록
     *
     * @param originalDocumentVo  원본 문서
     * @param trainingDocumentVos 전처리 문서 목록
     */
    @Override
    @Transactional
    public void registerDocument(OriginalDocumentVo originalDocumentVo, List<TrainingDocumentVo> trainingDocumentVos) {

        // 원본 문서 영속화
        OriginalDocument originalDocument = documentPersistencePort.saveOriginalDocumentPort(OriginalDocument.builder()
                .version(originalDocumentVo.getVersion())
                .docType(originalDocumentVo.getDocType())
                .categoryCode(originalDocumentVo.getCategoryCode())
                .name(originalDocumentVo.getName())
                .content(originalDocumentVo.getContent())
                .build());

        // 전처리 문서 목록 영속화
        trainingDocumentVos.forEach(trainingDocumentVo -> {
            documentPersistencePort.saveTrainingDocumentPort(TrainingDocument.builder()
                    .originalId(originalDocument.getOriginalId())
                    .docType(trainingDocumentVo.getDocType())
                    .categoryCode(trainingDocumentVo.getCategoryCode())
                    .version(trainingDocumentVo.getVersion())
                    .title(trainingDocumentVo.getTitle())
                    .subTitle(trainingDocumentVo.getSubTitle())
                    .thirdTitle(trainingDocumentVo.getThirdTitle())
                    .content(trainingDocumentVo.getContent())
                    .subContent(trainingDocumentVo.getSubContent())
                    .tokenSize(trainingDocumentVo.getTokenSize())
                    .build());
        });
    }
}
