package com.document.extractor.application.service;

import com.document.extractor.application.port.FilePersistencePort;
import com.document.extractor.application.command.CreateSourceCommand;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.port.SourcePersistencePort;
import com.document.extractor.application.usecase.SourceUseCase;
import com.document.extractor.application.vo.FileVo;
import com.document.extractor.domain.model.FileDetail;
import com.document.extractor.domain.model.Source;
import com.document.extractor.domain.model.SourcePattern;
import com.document.extractor.domain.model.SourcePrefix;
import com.document.extractor.domain.vo.PatternVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SourceService implements SourceUseCase {

    private final SourcePersistencePort sourcePersistencePort;
    private final FilePersistencePort filePersistencePort;
    private final ExtractPort extractPort;

    /**
     * 대상 문서 등록
     *
     * @param createSourceCommand 대상 문서 등록 Command
     */
    @Transactional
    @Override
    public void createSourcesUseCase(CreateSourceCommand createSourceCommand) {

        FileVo fileVo = createSourceCommand.getFile();

        // 파일 업로드
        FileDetail fileDetail = filePersistencePort.createFileDetail(FileDetail.builder()
                .originalFileName(fileVo.getOriginFileName())
                .fileName(fileVo.getFileName())
                .ip(fileVo.getIp())
                .filePath(fileVo.getFilePath())
                .fileSize(fileVo.getFileSize())
                .ext(fileVo.getExt())
                .url(fileVo.getUrl())
                .build());

        // Source 생성 및 영속화
        Source source = sourcePersistencePort.createSourcePort(Source.builder()
                .sourceType(createSourceCommand.getSourceType())
                .categoryCode(createSourceCommand.getCategoryCode())
                .name(fileVo.getOriginFileName())
                .content(extractPort.extractTextPort(fileDetail))
                .collectionId(createSourceCommand.getCollectionId())
                .fileDetailId(fileDetail.getFileDetailId())
                .maxTokenSize(createSourceCommand.getMaxTokenSize())
                .overlapSize(createSourceCommand.getOverlapSize())
                .isActive(false)
                .build());

        // SourcePattern 목록 생성 및 영속화
        for (int depth = 1; depth < createSourceCommand.getPatterns().size(); depth++) {
            PatternVo patternVo = createSourceCommand.getPatterns().get(depth);

            sourcePersistencePort.createSourcePatternPort(SourcePattern.builder()
                    .sourceId(source.getSourceId())
                    .tokenSize(patternVo.getTokenSize())
                    .depth(depth)
                    .sourcePrefixes(patternVo.getPrefixes().stream()
                            .map(prefixVo -> SourcePrefix.builder()
                                    .prefix(prefixVo.getPrefix())
                                    .isTitle(prefixVo.getIsTitle())
                                    .build())
                            .toList())
                    .build());
        }
    }
}
