package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.SourceEntity;
import com.document.extractor.adapter.out.repository.SourceRepository;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.SourcePersistencePort;
import com.document.extractor.domain.model.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourcePersistenceAdapter implements SourcePersistencePort {

    private final SourceRepository sourceRepository;

    /**
     * 대상 문서 등록
     *
     * @param source 대상 문서
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Source saveSourcePort(Source source) {

        SourceEntity sourceEntity;

        if (source.getSourceId() == null) {
            sourceEntity = sourceRepository.save(SourceEntity.fromDomain(source));
        } else {
            sourceEntity = sourceRepository.findById(source.getSourceId()).orElseThrow(NotFoundException::new);
            sourceEntity.update(source);
            sourceEntity = sourceRepository.save(sourceEntity);
        }

        return sourceEntity.toDomain();
    }

    /**
     * 대상 문서 조회
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Optional<Source> getSourcePort(Long sourceId) {
        return sourceRepository.findById(sourceId).map(SourceEntity::toDomain);
    }

    /**
     * 대상 문서 조회 (비관락)
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Optional<Source> getSourcePortWithLock(Long sourceId) {
        return sourceRepository.findBySourceId(sourceId).map(SourceEntity::toDomain);
    }

    /**
     * 배치 대상 문서 조회
     *
     * @return 대상 문서 목록
     */
    @Transactional
    @Override
    public List<Source> getActiveSourcesPort() {
        return sourceRepository.findByIsAutoTrueOrderBySourceId().stream()
                .map(SourceEntity::toDomain)
                .collect(Collectors.toList());
    }
}