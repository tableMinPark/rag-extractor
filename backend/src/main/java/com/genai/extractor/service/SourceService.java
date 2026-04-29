package com.genai.extractor.service;

import com.genai.extractor.constant.FileConst;
import com.genai.extractor.vo.CommonCodeVo;
import com.genai.extractor.vo.SourceVo;
import com.genai.extractor.repository.entity.SourceEntity;
import com.genai.extractor.enums.SelectType;
import com.genai.extractor.enums.SourceType;
import com.genai.extractor.repository.SourceRepository;
import com.genai.extractor.service.domain.model.Source;
import com.genai.extractor.service.domain.model.SourcePattern;
import com.genai.extractor.service.domain.model.SourcePrefix;
import com.genai.extractor.service.domain.model.SourceStopPattern;
import com.genai.extractor.repository.entity.FileDetailEntity;
import com.genai.extractor.service.vo.PatternVo;
import com.genai.extractor.service.vo.PrefixVo;
import com.genai.common.exception.NotFoundException;
import com.genai.common.repository.CommonCodeRepository;
import com.genai.common.repository.entity.CommonCodeEntity;
import com.genai.common.utils.StringUtil;
import com.genai.common.vo.UploadFile;
import com.genai.global.wrapper.PageWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final FileDetailService fileDetailService;
    private final ExtractService extractService;
    private final CommonCodeRepository commonCodeRepository;

    @Transactional
    public void createSource(String sourceType, String categoryCode, String collectionId,
                             Integer maxTokenSize, Integer overlapSize, List<PatternVo> patterns,
                             List<String> stopPatterns, String selectType, UploadFile uploadFile,
                             Boolean isAuto) {
        SourceType sourceTypeEnum = SourceType.find(sourceType.toUpperCase());
        SelectType selectTypeEnum = SelectType.find(selectType.toUpperCase());

        FileDetailEntity fileDetail = fileDetailService.saveFileDetail(uploadFile, FileConst.FILE_PERSIST_USER);

        String content = "";
        if (SourceType.FILE.equals(sourceTypeEnum)) {
            content = extractService.extractText(fileDetail);
        }

        List<SourcePattern> sourcePatterns = IntStream.range(0, patterns.size())
                .mapToObj(depth -> {
                    PatternVo patternVo = patterns.get(depth);
                    List<SourcePrefix> sourcePrefixes = IntStream.range(0, patternVo.getPrefixes().size())
                            .mapToObj(order -> {
                                PrefixVo prefixVo = patternVo.getPrefixes().get(order);
                                return SourcePrefix.builder()
                                        .prefix(prefixVo.getPrefix())
                                        .order(order)
                                        .isTitle(prefixVo.getIsTitle())
                                        .build();
                            }).toList();
                    return SourcePattern.builder()
                            .tokenSize(patternVo.getTokenSize())
                            .depth(depth)
                            .sourcePrefixes(sourcePrefixes)
                            .build();
                }).toList();

        List<SourceStopPattern> sourceStopPatterns = stopPatterns.stream()
                .map(prefix -> SourceStopPattern.builder().prefix(prefix).build())
                .toList();

        Source source = Source.builder()
                .sourceType(sourceTypeEnum)
                .selectType(selectTypeEnum)
                .version(0L)
                .categoryCode(categoryCode)
                .name(StringUtil.removeExtension(fileDetail.getOriginFileName()))
                .content(content)
                .collectionId(collectionId)
                .fileDetailId(fileDetail.getFileDetailId())
                .maxTokenSize(maxTokenSize)
                .overlapSize(overlapSize)
                .isAuto(isAuto)
                .isBatch(false)
                .sourcePatterns(sourcePatterns)
                .sourceStopPatterns(sourceStopPatterns)
                .build();

        saveSource(source);
    }

    @Transactional(readOnly = true)
    public List<SourceVo> getActiveSources() {
        return sourceRepository.findByIsBatchTrueOrderBySourceId().stream()
                .map(e -> SourceVo.of(e.toDomain()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SourceVo getSourceVo(Long sourceId) {
        return SourceVo.of(getSource(sourceId));
    }

    @Transactional(readOnly = true)
    public PageWrapper<SourceVo> getSources(int page, int size, String orderBy, String order,
                                            String keyword, String categoryCode) {
        Sort sort = Sort.by(order.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, orderBy);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        String searchKeyword = "%" + keyword.replace(" ", "%") + "%";

        Page<SourceEntity> sourceEntities;
        if ("".equals(categoryCode)) {
            if (keyword.isBlank()) sourceEntities = sourceRepository.findAll(pageable);
            else sourceEntities = sourceRepository.findAllByNameLike(searchKeyword, pageable);
        } else {
            if (keyword.isBlank()) sourceEntities = sourceRepository.findAllByCategoryCode(categoryCode, pageable);
            else sourceEntities = sourceRepository.findAllByCategoryCodeAndNameLike(categoryCode, searchKeyword, pageable);
        }

        return PageWrapper.<SourceVo>builder()
                .content(sourceEntities.stream().map(e -> SourceVo.of(e.toDomain())).toList())
                .isLast(sourceEntities.isLast())
                .pageNo(sourceEntities.getNumber() + 1)
                .pageSize(sourceEntities.getSize())
                .totalCount(sourceEntities.getTotalElements())
                .totalPages(sourceEntities.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommonCodeVo> getCategories() {
        return commonCodeRepository.findComnCodeByCodeGroupOrderBySortOrder("TRAIN").stream()
                .map(CommonCodeVo::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getSourceTotalCount() {
        return sourceRepository.count();
    }

    @Transactional
    public void deleteSource(Long sourceId) {
        SourceEntity sourceEntity = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"));
        sourceRepository.delete(sourceEntity);
    }

    @Transactional
    public void updateIsBatch(Long sourceId, boolean isBatch) {
        SourceEntity sourceEntity = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"));
        sourceEntity.updateIsBatch(isBatch);
    }

    public Source getSource(Long sourceId) {
        return sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"))
                .toDomain();
    }

    public Source getSourceWithLock(Long sourceId) {
        return sourceRepository.findBySourceId(sourceId)
                .orElseThrow(() -> new NotFoundException("대상 문서"))
                .toDomain();
    }

    public Source saveSource(Source source) {
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
}
