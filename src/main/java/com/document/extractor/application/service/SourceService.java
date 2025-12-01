package com.document.extractor.application.service;

import com.document.extractor.application.command.CreateSourceCommand;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.port.FilePersistencePort;
import com.document.extractor.application.port.SourcePersistencePort;
import com.document.extractor.application.usecase.SourceUseCase;
import com.document.extractor.application.vo.FileVo;
import com.document.extractor.domain.model.*;
import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.domain.vo.PrefixVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

        // 파일 목록 생성
        FileVo fileVo = createSourceCommand.getFile();

        // 파일 업로드
        FileDetail fileDetail = filePersistencePort.createFileDetail(FileDetail.builder()
                .originFileName(fileVo.getOriginFileName())
                .fileName(fileVo.getFileName())
                .ip(fileVo.getIp())
                .filePath(fileVo.getFilePath())
                .fileSize(fileVo.getFileSize())
                .ext(fileVo.getExt())
                .url(fileVo.getUrl())
                .build());

        String content = "";

        if (SourceType.FILE.equals(createSourceCommand.getSourceType())) {
            content = extractPort.extractTextPort(fileDetail);
        }

        // Source 생성 및 영속화
        Source source = sourcePersistencePort.createSourcePort(Source.builder()
                .sourceType(createSourceCommand.getSourceType().getCode())
                .selectType(createSourceCommand.getSelectType())
                .categoryCode(createSourceCommand.getCategoryCode())
                .name(fileVo.getOriginFileName())
                .content(content)
                .collectionId(createSourceCommand.getCollectionId())
                .fileDetailId(fileDetail.getFileDetailId())
                .maxTokenSize(createSourceCommand.getMaxTokenSize())
                .overlapSize(createSourceCommand.getOverlapSize())
                .isActive(false)
                .build());

        List<SourcePattern> sourcePatterns = new ArrayList<>();

        // SourcePattern 목록 생성 및 영속화
        for (int depth = 1; depth <= createSourceCommand.getPatterns().size(); depth++) {
            PatternVo patternVo = createSourceCommand.getPatterns().get(depth - 1);

            List<SourcePrefix> sourcePrefixes = new ArrayList<>();
            for (int order = 1; order <= patternVo.getPrefixes().size(); order++) {
                PrefixVo prefixVo = patternVo.getPrefixes().get(order - 1);
                sourcePrefixes.add(SourcePrefix.builder()
                        .prefix(prefixVo.getPrefix())
                        .order(order)
                        .isTitle(prefixVo.getIsTitle())
                        .build());
            }

            sourcePatterns.add(SourcePattern.builder()
                    .sourceId(source.getSourceId())
                    .tokenSize(patternVo.getTokenSize())
                    .depth(depth)
                    .sourcePrefixes(sourcePrefixes)
                    .build());
        }

        List<SourceStopPattern> sourceStopPatterns = createSourceCommand.getStopPatterns().stream()
                        .map(prefix -> SourceStopPattern.builder()
                                .sourceId(source.getSourceId())
                                .prefix(prefix)
                                .build())
                        .toList();

        sourcePersistencePort.createSourcePatternPort(sourcePatterns, sourceStopPatterns);
    }
}
