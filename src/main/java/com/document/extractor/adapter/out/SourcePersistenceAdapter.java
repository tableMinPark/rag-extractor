package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.SourceEntity;
import com.document.extractor.adapter.out.entity.SourcePatternEntity;
import com.document.extractor.adapter.out.entity.SourcePrefixEntity;
import com.document.extractor.adapter.out.entity.SourceStopPatternEntity;
import com.document.extractor.adapter.out.repository.*;
import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.port.SourcePersistencePort;
import com.document.extractor.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SourcePersistenceAdapter implements SourcePersistencePort {

    private final SourceRepository sourceRepository;
    private final SourcePatternRepository sourcePatternRepository;
    private final SourceStopPatternRepository sourceStopPatternRepository;
    private final PassageRepository passageRepository;
    private final ChunkRepository chunkRepository;

    /**
     * 대상 문서 등록
     *
     * @param source 대상 문서
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Source createSourcePort(Source source) {

        SourceEntity sourceEntity = sourceRepository.save(SourceEntity.builder()
                .version(source.getVersion())
                .sourceType(source.getSourceType())
                .selectType(source.getSelectType().getCode())
                .categoryCode(source.getCategoryCode())
                .name(source.getName())
                .content(source.getContent())
                .collectionId(source.getCollectionId())
                .fileDetailId(source.getFileDetailId())
                .maxTokenSize(source.getMaxTokenSize())
                .overlapSize(source.getOverlapSize())
                .isActive(source.getIsActive())
                .build());

        return Source.builder()
                .sourceId(sourceEntity.getSourceId())
                .version(sourceEntity.getVersion())
                .sourceType(sourceEntity.getSourceType())
                .selectType(SelectType.find(sourceEntity.getSelectType()))
                .categoryCode(sourceEntity.getCategoryCode())
                .name(sourceEntity.getName())
                .content(sourceEntity.getContent())
                .collectionId(sourceEntity.getCollectionId())
                .fileDetailId(sourceEntity.getFileDetailId())
                .maxTokenSize(sourceEntity.getMaxTokenSize())
                .overlapSize(sourceEntity.getOverlapSize())
                .isActive(sourceEntity.getIsActive())
                .sysCreateDt(sourceEntity.getSysCreateDt())
                .sysModifyDt(sourceEntity.getSysModifyDt())
                .sourcePatterns(Collections.emptyList())
                .sourceStopPatterns(Collections.emptyList())
                .build();
    }

    /**
     * 대상 문서 패턴 등록
     *
     * @param sourcePatterns     대상 문서 패턴
     * @param sourceStopPatterns 대상 문서 중단 패턴
     */
    @Transactional
    @Override
    public void createSourcePatternPort(List<SourcePattern> sourcePatterns, List<SourceStopPattern> sourceStopPatterns) {

        sourcePatternRepository.saveAll(sourcePatterns.stream()
                .map(sourcePattern -> SourcePatternEntity.builder()
                        .sourceId(sourcePattern.getSourceId())
                        .tokenSize(sourcePattern.getTokenSize())
                        .depth(sourcePattern.getDepth())
                        .sourcePrefixes(sourcePattern.getSourcePrefixes().stream()
                                .map(sourcePrefix -> SourcePrefixEntity.builder()
                                        .prefix(sourcePrefix.getPrefix())
                                        .order(sourcePrefix.getOrder())
                                        .isTitle(sourcePrefix.getIsTitle())
                                        .build())
                                .toList())
                        .build())
                .toList());

        sourceStopPatternRepository.saveAll(sourceStopPatterns.stream()
                .map(sourceStopPattern -> SourceStopPatternEntity.builder()
                        .sourceId(sourceStopPattern.getSourceId())
                        .prefix(sourceStopPattern.getPrefix())
                        .build())
                .toList());
    }

    /**
     * 패시지 저장
     *
     * @param passage 패시지
     * @return 패시지
     */
    @Transactional
    @Override
    public Passage createPassagePort(Passage passage) {
        return null;
    }

    /**
     * 청크 저장
     *
     * @param chunk 청크
     * @return 청크
     */
    @Transactional
    @Override
    public Chunk createChunkPort(Chunk chunk) {
        return null;
    }
}