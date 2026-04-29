package com.genai.extractor.service;

import com.genai.extractor.vo.ChunkResultVo;
import com.genai.extractor.vo.ChunkVo;
import com.genai.extractor.vo.PassageVo;
import com.genai.extractor.vo.SourceVo;
import com.genai.extractor.enums.ExtractType;
import com.genai.extractor.enums.SelectType;
import com.genai.extractor.enums.SourceType;
import com.genai.extractor.enums.UpdateState;
import com.genai.common.exception.InvalidSourceTypeException;
import com.genai.extractor.service.domain.factory.PassageFactory;
import com.genai.extractor.service.domain.model.*;
import com.genai.extractor.service.vo.PassageOptionVo;
import com.genai.extractor.service.vo.PatternVo;
import com.genai.extractor.service.vo.PrefixVo;
import com.genai.extractor.repository.entity.ChunkEntity;
import com.genai.extractor.repository.entity.FileDetailEntity;
import com.genai.extractor.repository.entity.PassageEntity;
import com.genai.extractor.repository.ChunkRepository;
import com.genai.extractor.repository.PassageRepository;
import com.genai.common.exception.NotFoundException;
import com.genai.common.utils.HtmlUtil;
import com.genai.common.vo.UploadFile;
import com.genai.global.wrapper.PageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkService {

    private final ExtractService extractService;
    private final SourceService sourceService;
    private final PassageService passageService;
    private final FileDetailService fileDetailService;
    private final ChunkRepository chunkRepository;
    private final PassageRepository passageRepository;

    public ChunkResultVo chunkFile(String extractType, String selectType, List<PatternVo> patterns,
                                   List<String> stopPatterns, int maxTokenSize, int overlapSize, UploadFile uploadFile) {
        List<SourcePattern> sourcePatterns = buildSourcePatterns(patterns);
        List<SourceStopPattern> sourceStopPatterns = buildSourceStopPatterns(stopPatterns);

        SelectType selectTypeEnum = SelectType.valueOf(selectType.toUpperCase());
        ExtractType extractTypeEnum = ExtractType.valueOf(extractType.toUpperCase());

        FileDetailEntity fileDetail = FileDetailEntity.builder()
                .originFileName(uploadFile.getOriginFileName())
                .fileName(uploadFile.getFileName())
                .ip("127.0.0.1")
                .filePath(uploadFile.getFilePath())
                .fileSize(uploadFile.getFileSize())
                .ext(uploadFile.getExt())
                .url(uploadFile.getUrl())
                .build();

        Document document = extractService.extractFile(fileDetail, extractTypeEnum.getCode());

        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(sourcePatterns)
                .stopPatterns(sourceStopPatterns)
                .selectType(selectTypeEnum)
                .build();

        List<Passage> currentPassages = SelectType.TOKEN.equals(selectTypeEnum)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, maxTokenSize)
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            currentPassage.connectSource(null, document.getName());
            currentPassage.update(null, sortOrder);
        }

        List<Chunk> chunks = new ArrayList<>();
        currentPassages.forEach(passage -> chunks.addAll(passage.chunking(maxTokenSize, overlapSize)));

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .previousPassages(Collections.emptyList())
                .currentPassages(currentPassages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    public ChunkResultVo chunkRepo(String extractType, String selectType, List<PatternVo> patterns,
                                   List<String> stopPatterns, int maxTokenSize, int overlapSize, String uri) {
        List<SourcePattern> sourcePatterns = buildSourcePatterns(patterns);
        List<SourceStopPattern> sourceStopPatterns = buildSourceStopPatterns(stopPatterns);

        SelectType selectTypeEnum = SelectType.valueOf(selectType.toUpperCase());
        ExtractType extractTypeEnum = ExtractType.valueOf(extractType.toUpperCase());

        Document document = extractService.readRepoDocument(uri, extractTypeEnum.getCode());

        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(sourcePatterns)
                .stopPatterns(sourceStopPatterns)
                .selectType(selectTypeEnum)
                .build();

        List<Passage> currentPassages = SelectType.TOKEN.equals(selectTypeEnum)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, maxTokenSize)
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            currentPassage.connectSource(null, document.getName());
            currentPassage.update(null, sortOrder);
        }

        List<Chunk> chunks = new ArrayList<>();
        currentPassages.forEach(passage -> chunks.addAll(passage.chunking(maxTokenSize, overlapSize)));

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .previousPassages(Collections.emptyList())
                .currentPassages(currentPassages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public ChunkResultVo chunkSource(long sourceId) {
        Source source = sourceService.getSource(sourceId);
        source.nextVersion();

        FileDetailEntity fileDetail = fileDetailService.getFileDetail(source.getFileDetailId());

        Document document;
        if (SourceType.FILE.equals(source.getSourceType())) {
            document = extractService.extractFile(fileDetail, ExtractType.HTML.getCode());
        } else if (SourceType.REPO.equals(source.getSourceType())) {
            document = extractService.readRepoDocument(fileDetail.getUrl(), ExtractType.HTML.getCode());
        } else throw new InvalidSourceTypeException();

        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(source.getSourcePatterns())
                .stopPatterns(source.getSourceStopPatterns())
                .selectType(source.getSelectType())
                .build();

        List<Passage> currentPassages = SelectType.TOKEN.equals(source.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, source.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            currentPassage.connectSource(source.getSourceId(), source.getName());
            currentPassage.update(source.getVersion(), sortOrder);
        }

        List<Passage> previousPassages = passageService.getPassagesByVersion(source.getSourceId(), source.getPreviousVersion());

        Passage.compareDiff(previousPassages, currentPassages);

        for (Passage previousPassage : previousPassages) {
            if (UpdateState.STAY.equals(previousPassage.getUpdateState()) || UpdateState.INSERT.equals(previousPassage.getUpdateState())) {
                for (Passage currentPassage : currentPassages) {
                    if (UpdateState.STAY.equals(currentPassage.getUpdateState()) && currentPassage.getParentSortOrder() == null) {
                        currentPassage.setParentSortOrder(previousPassage.getSortOrder());
                        break;
                    }
                }
            }
        }

        List<Chunk> chunks = new ArrayList<>();
        if (source.isFirstVersion()) {
            for (Passage passage : currentPassages) {
                chunks.addAll(passage.chunking(source.getMaxTokenSize(), source.getOverlapSize()));
            }
        } else {
            for (Passage passage : currentPassages) {
                switch (passage.getUpdateState()) {
                    case CHANGE, STAY -> {
                        if (passage.getParentSortOrder() != null) {
                            chunks.addAll(getChunkBySortOrderAndVersion(passage.getSourceId(), passage.getParentSortOrder(), source.getPreviousVersion()).stream()
                                    .peek(chunk -> chunk.update(passage.getPassageId(), passage.getVersion()))
                                    .toList());
                        }
                    }
                    case INSERT -> chunks.addAll(passage.chunking(source.getMaxTokenSize(), source.getOverlapSize()));
                }
            }
        }

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .source(SourceVo.of(source))
                .previousPassages(previousPassages.stream().map(PassageVo::of).toList())
                .currentPassages(currentPassages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    @Transactional
    public ChunkResultVo chunkBatch(long sourceId) {
        Source source = sourceService.getSourceWithLock(sourceId);
        source.nextVersion();

        FileDetailEntity fileDetail = fileDetailService.getFileDetail(source.getFileDetailId());

        Document document;
        if (SourceType.FILE.equals(source.getSourceType())) {
            document = extractService.extractFile(fileDetail, ExtractType.HTML.getCode());
        } else if (SourceType.REPO.equals(source.getSourceType())) {
            document = extractService.readRepoDocument(fileDetail.getUrl(), ExtractType.HTML.getCode());
        } else throw new InvalidSourceTypeException();

        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(source.getSourcePatterns())
                .stopPatterns(source.getSourceStopPatterns())
                .selectType(source.getSelectType())
                .build();

        List<Passage> currentPassages = SelectType.TOKEN.equals(source.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, source.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            currentPassage.connectSource(source.getSourceId(), source.getName());
            currentPassage.update(source.getVersion(), sortOrder);
        }

        List<Passage> previousPassages = passageService.getPassagesByVersion(source.getSourceId(), source.getPreviousVersion());

        Passage.compareDiff(previousPassages, currentPassages);

        for (Passage previousPassage : previousPassages) {
            if (UpdateState.STAY.equals(previousPassage.getUpdateState()) || UpdateState.INSERT.equals(previousPassage.getUpdateState())) {
                for (Passage currentPassage : currentPassages) {
                    if (UpdateState.STAY.equals(currentPassage.getUpdateState()) && currentPassage.getParentSortOrder() == null) {
                        currentPassage.setParentSortOrder(previousPassage.getSortOrder());
                        break;
                    }
                }
            }
        }

        Source persistSource = sourceService.saveSource(source);
        List<Passage> persistPreviousPassages = passageService.savePassages(previousPassages);
        List<Passage> persistCurrentPassages = passageService.savePassages(currentPassages);

        List<Chunk> chunks = new ArrayList<>();
        if (persistSource.isFirstVersion()) {
            for (Passage passage : persistCurrentPassages) {
                chunks.addAll(passage.chunking(persistSource.getMaxTokenSize(), persistSource.getOverlapSize()));
            }
        } else {
            for (Passage passage : persistCurrentPassages) {
                switch (passage.getUpdateState()) {
                    case CHANGE, STAY -> {
                        if (passage.getParentSortOrder() != null) {
                            chunks.addAll(getChunkBySortOrderAndVersion(passage.getSourceId(), passage.getParentSortOrder(), persistSource.getPreviousVersion()).stream()
                                    .peek(chunk -> chunk.update(passage.getPassageId(), passage.getVersion()))
                                    .toList());
                        }
                    }
                    case INSERT ->
                            chunks.addAll(passage.chunking(persistSource.getMaxTokenSize(), persistSource.getOverlapSize()));
                }
            }
        }

        List<Chunk> persistChunks = saveChunks(chunks);

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .source(SourceVo.of(persistSource))
                .previousPassages(persistPreviousPassages.stream().map(PassageVo::of).toList())
                .currentPassages(persistCurrentPassages.stream().map(PassageVo::of).toList())
                .chunks(persistChunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    @Transactional
    public void createChunk(Long passageId, String title, String subTitle, String thirdTitle,
                            String content, String subContent) {
        Passage passage = passageService.getPassage(passageId);

        String cleanContent = HtmlUtil.removeHtmlExceptTable(content);
        String compactContent = HtmlUtil.convertTableHtmlToMarkdown(cleanContent);

        Chunk chunk = Chunk.builder()
                .passageId(passage.getPassageId())
                .version(passage.getVersion())
                .title(title)
                .subTitle(subTitle)
                .thirdTitle(thirdTitle)
                .content(content)
                .compactContent(compactContent)
                .tokenSize(content.length())
                .compactTokenSize(compactContent.length())
                .subContent(subContent)
                .build();

        saveChunk(chunk);
    }

    @Transactional(readOnly = true)
    public ChunkVo getChunk(Long chunkId) {
        return ChunkVo.of(chunkRepository.findById(chunkId)
                .orElseThrow(() -> new NotFoundException("청크"))
                .toDomain());
    }

    @Transactional(readOnly = true)
    public PageWrapper<ChunkVo> getChunks(int page, int size, long passageId) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<ChunkEntity> chunkEntities = chunkRepository.findByPassageId(passageId, pageable);

        return PageWrapper.<ChunkVo>builder()
                .content(chunkEntities.stream().map(e -> ChunkVo.of(e.toDomain())).toList())
                .isLast(chunkEntities.isLast())
                .pageNo(chunkEntities.getNumber() + 1)
                .pageSize(chunkEntities.getSize())
                .totalCount(chunkEntities.getTotalElements())
                .totalPages(chunkEntities.getTotalPages())
                .build();
    }

    @Transactional
    public void updateChunk(Long chunkId, String title, String subTitle, String thirdTitle,
                            String content, String subContent) {
        Chunk chunk = chunkRepository.findById(chunkId)
                .orElseThrow(() -> new NotFoundException("청크"))
                .toDomain();

        String cleanContent = HtmlUtil.removeHtmlExceptTable(content);
        String compactContent = HtmlUtil.convertTableHtmlToMarkdown(cleanContent);

        chunk.update(title, subTitle, thirdTitle, content, subContent, compactContent,
                content.length(), compactContent.length());

        saveChunk(chunk);
    }

    @Transactional
    public void deleteChunk(Long chunkId) {
        chunkRepository.deleteById(chunkId);
    }

    @Transactional(readOnly = true)
    public long getChunkTotalCount() {
        return chunkRepository.count();
    }

    private Chunk saveChunk(Chunk chunk) {
        ChunkEntity chunkEntity;
        if (chunk.getChunkId() == null) {
            chunkEntity = chunkRepository.save(ChunkEntity.fromDomain(chunk));
        } else {
            chunkEntity = chunkRepository.findById(chunk.getChunkId()).orElseThrow(() -> new NotFoundException("청크"));
            chunkEntity.update(chunk);
            chunkEntity = chunkRepository.save(chunkEntity);
        }
        return chunkEntity.toDomain();
    }

    private List<Chunk> saveChunks(List<Chunk> chunks) {
        List<ChunkEntity> chunkEntities = chunks.stream()
                .map(chunk -> chunk.getChunkId() == null
                        ? ChunkEntity.fromDomain(chunk)
                        : chunkRepository.findById(chunk.getChunkId()).orElseThrow(() -> new NotFoundException("청크")).update(chunk))
                .toList();
        return chunkRepository.saveAll(chunkEntities).stream().map(ChunkEntity::toDomain).toList();
    }

    private List<Chunk> getChunkBySortOrderAndVersion(Long sourceId, Integer sortOrder, Long version) {
        PassageEntity passageEntity = passageRepository.findBySourceIdAndSortOrderAndVersion(sourceId, sortOrder, version)
                .orElseThrow(() -> new NotFoundException("패시지"));
        return chunkRepository.findByPassageIdAndVersion(passageEntity.getPassageId(), passageEntity.getVersion()).stream()
                .map(ChunkEntity::toDomain)
                .toList();
    }

    private List<SourcePattern> buildSourcePatterns(List<PatternVo> patterns) {
        return IntStream.range(0, patterns.size())
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
    }

    private List<SourceStopPattern> buildSourceStopPatterns(List<String> stopPatterns) {
        return stopPatterns.stream()
                .map(prefix -> SourceStopPattern.builder().prefix(prefix).build())
                .toList();
    }
}
