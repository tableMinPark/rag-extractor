package com.genai.extractor.adapter.out;

import com.genai.common.repository.entity.CommonCodeEntity;
import com.genai.common.repository.CommonCodeRepository;
import com.genai.extractor.adapter.out.entity.SourceEntity;
import com.genai.extractor.adapter.out.repository.SourceRepository;
import com.genai.common.exception.NotFoundException;
import com.genai.extractor.application.port.SourcePersistencePort;
import com.genai.global.wrapper.PageWrapper;
import com.genai.extractor.domain.model.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourcePersistenceAdapter implements SourcePersistencePort {

    private final SourceRepository sourceRepository;
    private final CommonCodeRepository commonCodeRepository;

    /**
     * 대상 문서 등록
     *
     * @param source 대상 문서
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Source saveSourcePort(Source source) {

        CommonCodeEntity categoryEntity = commonCodeRepository.findByCode(source.getCategoryCode())
                .orElseThrow(() -> new NotFoundException("대상 문서 카테고리"));

        SourceEntity sourceEntity;

        if (source.getSourceId() == null) {
            sourceEntity = sourceRepository.save(SourceEntity.fromDomain(source, categoryEntity));
        } else {
            sourceEntity = sourceRepository.findById(source.getSourceId()).orElseThrow(() -> new NotFoundException("대상 문서"));
            sourceEntity.update(source, categoryEntity);
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
    public Source getSourcePort(Long sourceId) {
        return sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"))
                .toDomain();
    }

    /**
     * 대상 문서 조회 (비관락)
     *
     * @param sourceId 대상 문서 ID
     * @return 대상 문서
     */
    @Transactional
    @Override
    public Source getSourceWithLockPort(Long sourceId) {
        return sourceRepository.findBySourceId(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"))
                .toDomain();
    }

    /**
     * 대상 문서 목록 조회
     *
     * @param page         페이지
     * @param size         사이즈
     * @param orderBy      정렬 필드
     * @param order        정렬 방향 ( asc | desc )
     * @param keyword      키워드
     * @param categoryCode 카테고리 코드
     * @return 대상 문서 목록
     */
    @Transactional
    @Override
    public PageWrapper<Source> getSourcesPort(int page, int size, String orderBy, String order, String keyword, String categoryCode) {
        Sort sort = Sort.by(order.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, orderBy);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        String searchKeyword = "%" + keyword.replace(" ", "%") + "%";

        Page<SourceEntity> sourceEntities;
        if ("ALL".equals(categoryCode)) {
            if (keyword.isBlank()) sourceEntities = sourceRepository.findAll(pageable);
            else sourceEntities = sourceRepository.findAllByNameLike(searchKeyword, pageable);
        } else {
            if (keyword.isBlank()) sourceEntities = sourceRepository.findAllByCategoryCode(categoryCode, pageable);
            else sourceEntities = sourceRepository.findAllByCategoryCodeAndNameLike(categoryCode, searchKeyword, pageable);
        }

        return PageWrapper.<Source>builder()
                .content(sourceEntities.stream().map(SourceEntity::toDomain).toList())
                .isLast(sourceEntities.isLast())
                .pageNo(sourceEntities.getNumber() + 1)
                .pageSize(sourceEntities.getSize())
                .totalCount(sourceEntities.getTotalElements())
                .totalPages(sourceEntities.getTotalPages())
                .build();
    }

    /**
     * 배치 대상 문서 목록 조회
     *
     * @return 대상 문서 목록
     */
    @Transactional
    @Override
    public List<Source> getActiveSourcesPort() {
        return sourceRepository.findByIsBatchTrueOrderBySourceId().stream()
                .map(SourceEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 총 대상 문서 수 조회
     *
     * @return 총 문서 수
     */
    @Transactional(readOnly = true)
    @Override
    public long getSourceTotalCountPort() {
        return sourceRepository.count();
    }

    /**
     * 대상 문서 삭제
     *
     * @param sourceId 대상 문서 ID
     */
    @Transactional
    @Override
    public void deleteSourcePort(Long sourceId) {
        SourceEntity sourceEntity = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"));
        sourceRepository.delete(sourceEntity);
    }

    /**
     * 대상 문서 배치 여부 수정
     *
     * @param sourceId 대상 문서 ID
     * @param isBatch  배치 여부
     */
    @Transactional
    @Override
    public void updateIsBatchPort(Long sourceId, boolean isBatch) {
        SourceEntity sourceEntity = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"));
        sourceEntity.updateIsBatch(isBatch);
    }
}