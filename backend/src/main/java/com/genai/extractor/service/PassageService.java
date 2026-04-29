package com.genai.extractor.service;

import com.genai.extractor.vo.PassageVo;
import com.genai.extractor.repository.entity.PassageEntity;
import com.genai.extractor.repository.entity.SourceEntity;
import com.genai.extractor.repository.ChunkRepository;
import com.genai.extractor.repository.PassageRepository;
import com.genai.extractor.repository.SourceRepository;
import com.genai.extractor.service.domain.model.Passage;
import com.genai.common.exception.NotFoundException;
import com.genai.global.wrapper.PageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassageService {

    private final SourceRepository sourceRepository;
    private final PassageRepository passageRepository;
    private final ChunkRepository chunkRepository;

    @Transactional(readOnly = true)
    public PassageVo getPassageVo(Long passageId) {
        return PassageVo.of(getPassage(passageId));
    }

    @Transactional(readOnly = true)
    public PageWrapper<PassageVo> getPassageVos(int page, int size, long sourceId) {
        SourceEntity sourceEntity = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"));

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<PassageEntity> passageEntities = passageRepository.findBySourceIdAndVersionOrderBySortOrderAsc(sourceId, sourceEntity.getVersion(), pageable);

        return PageWrapper.<PassageVo>builder()
                .content(passageEntities.stream().map(e -> PassageVo.of(e.toDomain())).toList())
                .isLast(passageEntities.isLast())
                .pageNo(passageEntities.getNumber() + 1)
                .pageSize(passageEntities.getSize())
                .totalCount(passageEntities.getTotalElements())
                .totalPages(passageEntities.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public long getPassageTotalCount() {
        return passageRepository.count();
    }

    public Passage getPassage(Long passageId) {
        return passageRepository.findById(passageId)
                .orElseThrow(() -> new NotFoundException("패시지"))
                .toDomain();
    }

    public List<Passage> getPassagesByVersion(Long sourceId, long version) {
        return passageRepository.findBySourceIdAndVersionOrderBySortOrderAsc(sourceId, version).stream()
                .map(PassageEntity::toDomain)
                .toList();
    }

    public Passage savePassage(Passage passage) {
        PassageEntity passageEntity;
        if (passage.getPassageId() == null) {
            passageEntity = passageRepository.save(PassageEntity.fromDomain(passage));
        } else {
            passageEntity = passageRepository.findById(passage.getPassageId()).orElseThrow(() -> new NotFoundException("패시지"));
            passageEntity.update(passage);
            passageEntity = passageRepository.save(passageEntity);
        }
        return passageEntity.toDomain();
    }

    public List<Passage> savePassages(List<Passage> passages) {
        List<PassageEntity> passageEntities = passages.stream()
                .map(passage -> passage.getPassageId() == null
                        ? PassageEntity.fromDomain(passage)
                        : passageRepository.findById(passage.getPassageId()).orElseThrow(() -> new NotFoundException("패시지")).update(passage))
                .toList();
        return passageRepository.saveAll(passageEntities).stream().map(PassageEntity::toDomain).toList();
    }
}
