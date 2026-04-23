package com.genai.embed.service;

import com.genai.embed.repository.CollectionRepository;
import com.genai.embed.repository.entity.DocumentEntity;
import com.genai.extractor.adapter.in.dto.response.ChunkBatchResponseDto;
import com.genai.extractor.adapter.out.repository.ChunkRepository;
import com.genai.extractor.adapter.out.repository.PassageRepository;
import com.genai.extractor.adapter.out.entity.ChunkEntity;
import com.genai.extractor.adapter.out.entity.PassageEntity;
import com.genai.extractor.application.port.SourcePersistencePort;
import com.genai.extractor.domain.model.Source;
import com.genai.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbedService {

    private final SourcePersistencePort sourcePersistencePort;
    private final PassageRepository passageRepository;
    private final ChunkRepository chunkRepository;
    private final CollectionRepository collectionRepository;

    /**
     * 특정 대상 문서의 청크를 색인
     *
     * @param sourceId 대상 문서 ID
     * @return 색인 결과
     */
    @Transactional(readOnly = true)
    public ChunkBatchResponseDto embedSource(Long sourceId) {
        Source source = sourcePersistencePort.getSourcePort(sourceId);

        if (source.getCollectionId() == null || source.getCollectionId().isBlank()) {
            throw new NotFoundException("컬렉션 ID");
        }

        String collectionId = source.getCollectionId();

        collectionRepository.findCollectionByCollectionId(collectionId)
                .orElseThrow(() -> new NotFoundException("컬렉션 (" + collectionId + ")"));

        List<PassageEntity> passages = passageRepository
                .findBySourceIdAndVersionOrderBySortOrderAsc(sourceId, source.getVersion());

        List<DocumentEntity> documentEntities = passages.stream()
                .flatMap(passage -> {
                    List<ChunkEntity> chunks = chunkRepository
                            .findByPassageIdAndVersion(passage.getPassageId(), source.getVersion());
                    return chunks.stream().map(chunk -> toDocumentEntity(source, passage, chunk));
                })
                .toList();

        List<DocumentEntity> vectorized = collectionRepository.convertVector(collectionId, documentEntities);
        collectionRepository.createIndex(collectionId, vectorized);

        int passageCount = passages.size();
        int chunkCount = documentEntities.size();

        log.info("색인 완료 - sourceId={}, collectionId={}, passages={}, chunks={}",
                sourceId, collectionId, passageCount, chunkCount);

        return ChunkBatchResponseDto.builder()
                .isConvertError(false)
                .fileName(source.getName())
                .version(source.getVersion())
                .totalPassageCount(passageCount)
                .totalChunkCount(chunkCount)
                .build();
    }

    /**
     * 특정 대상 문서의 색인 삭제
     *
     * @param sourceId 대상 문서 ID
     */
    @Transactional(readOnly = true)
    public void deleteEmbedSource(Long sourceId) {
        Source source = sourcePersistencePort.getSourcePort(sourceId);

        if (source.getCollectionId() == null || source.getCollectionId().isBlank()) {
            return;
        }

        String collectionId = source.getCollectionId();

        List<PassageEntity> passages = passageRepository
                .findBySourceIdAndVersionOrderBySortOrderAsc(sourceId, source.getVersion());

        List<String> chunkIds = passages.stream()
                .flatMap(passage -> chunkRepository
                        .findByPassageIdAndVersion(passage.getPassageId(), source.getVersion())
                        .stream()
                        .map(chunk -> String.valueOf(chunk.getChunkId())))
                .toList();

        collectionRepository.deleteIndex(collectionId, chunkIds);

        log.info("색인 삭제 완료 - sourceId={}, collectionId={}, chunks={}", sourceId, collectionId, chunkIds.size());
    }

    private DocumentEntity toDocumentEntity(Source source, PassageEntity passage, ChunkEntity chunk) {
        String context = buildContext(passage, chunk);
        return DocumentEntity.builder()
                .chunkId(chunk.getChunkId())
                .passageId(passage.getPassageId())
                .sourceId(source.getSourceId())
                .fileDetailId(source.getFileDetailId())
                .name(source.getName())
                .title(chunk.getTitle())
                .subTitle(chunk.getSubTitle())
                .thirdTitle(chunk.getThirdTitle())
                .compactContent(chunk.getCompactContent())
                .content(chunk.getContent())
                .subContent(chunk.getSubContent())
                .context(context)
                .categoryCode(source.getCategoryCode())
                .sourceType(source.getSourceType() != null ? source.getSourceType().getCode() : null)
                .sysCreateDt(chunk.getSysCreateDt())
                .sysModifyDt(chunk.getSysModifyDt())
                .build();
    }

    private String buildContext(PassageEntity passage, ChunkEntity chunk) {
        StringBuilder sb = new StringBuilder();
        if (passage.getTitle() != null && !passage.getTitle().isBlank()) {
            sb.append(passage.getTitle()).append(" ");
        }
        if (chunk.getCompactContent() != null && !chunk.getCompactContent().isBlank()) {
            sb.append(chunk.getCompactContent());
        } else if (chunk.getContent() != null) {
            sb.append(chunk.getContent());
        }
        return sb.toString().trim();
    }
}
