package com.extractor.adapter.out;

import com.extractor.adapter.out.entity.LawContentEntity;
import com.extractor.adapter.out.entity.LawDocumentEntity;
import com.extractor.adapter.out.repository.LawAutoLinkRepository;
import com.extractor.adapter.out.repository.LawContentRepository;
import com.extractor.adapter.out.repository.LawDocumentRepository;
import com.extractor.application.exception.NotFoundDocumentException;
import com.extractor.application.port.LawPersistencePort;
import com.extractor.domain.model.law.LawContent;
import com.extractor.domain.model.law.LawDocument;
import com.extractor.domain.model.law.LawLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LawPersistenceAdapter implements LawPersistencePort {

    private final LawDocumentRepository lawDocumentRepository;

    private final LawContentRepository lawContentRepository;

    private final LawAutoLinkRepository lawAutoLinkRepository;

    /**
     * 법령 문서 조회
     * @param lawId 법령 ID
     */
    @Override
    public LawDocument getLawDocumentsPort(Long lawId) {

        LawDocumentEntity lawDocumentEntity = lawDocumentRepository.findByLawId(lawId)
                .orElseThrow(NotFoundDocumentException::new);

        List<LawContentEntity> lawContentEntities = lawContentRepository.findByLawId(lawId);

        return LawDocument.builder()
                .lawId(lawDocumentEntity.getLawId())
                .lawName(lawDocumentEntity.getLawName())
                .lawContents(lawContentEntities.stream()
                        .map(lawContentEntity -> LawContent.builder()
                                .lawContentId(lawContentEntity.getLawContentId())
                                .lawId(lawContentEntity.getLawId())
                                .version(lawContentEntity.getVersion())
                                .contentType(lawContentEntity.getContentType())
                                .categoryCode(lawContentEntity.getCategoryCode())
                                .arrange(lawContentEntity.getArrange())
                                .simpleTitle(lawContentEntity.getSimpleTitle())
                                .title(lawContentEntity.getTitle())
                                .content(lawContentEntity.getContent())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 법령 본문 ID, 버전 기준 법령 연결 정보 목록 조회
     * @param lawContentId 법령 본문 ID
     */
    public List<LawLink> getLawLinksPort(Long lawContentId, Integer version) {
        return lawAutoLinkRepository.findByLawContentIdAndVersion(lawContentId, version).stream()
                .map(lawLinkEntity -> LawLink.builder()
                        .lawLinkId(lawLinkEntity.getLawLinkId())
                        .lawContentId(lawLinkEntity.getLawContentId())
                        .lawId(lawLinkEntity.getLawId())
                        .version(lawLinkEntity.getVersion())
                        .content(lawLinkEntity.getContent())
                        .linkTag(lawLinkEntity.getLinkTag())
                        .build())
                .collect(Collectors.toList());
    }
}
