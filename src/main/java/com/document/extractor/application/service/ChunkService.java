package com.document.extractor.application.service;

import com.document.extractor.adapter.out.SourcePersistenceAdapter;
import com.document.extractor.application.command.ChunkBatchCommand;
import com.document.extractor.application.command.ChunkCommand;
import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.enums.ExtractType;
import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.DocumentReadPort;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.port.FilePersistencePort;
import com.document.extractor.application.port.SourcePersistencePort;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.vo.ChunkResultVo;
import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.vo.SourceVo;
import com.document.extractor.domain.factory.PassageFactory;
import com.document.extractor.domain.model.*;
import com.document.extractor.domain.vo.PassageOptionVo;
import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.domain.vo.PrefixVo;
import com.document.global.utils.StringUtil;
import com.document.global.vo.UploadFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ChunkService implements ChunkUseCase {

    private final SourcePersistencePort sourcePersistencePort;
    private final FilePersistencePort filePersistencePort;
    private final ExtractPort extractPort;
    private final DocumentReadPort documentReadPort;
    private final SourcePersistenceAdapter sourcePersistenceAdapter;

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     */
    @Override
    public ChunkResultVo chunkFileUseCase(ChunkFileCommand command) {
        // 전처리 패턴
        List<SourcePattern> sourcePatterns = IntStream.range(0, command.getPatterns().size())
                .mapToObj(depth -> {
                    PatternVo patternVo = command.getPatterns().get(depth);
                    List<SourcePrefix> sourcePrefixes = IntStream.range(0, patternVo.getPrefixes().size())
                            .mapToObj(order -> {
                                PrefixVo prefixVo = patternVo.getPrefixes().get(order);

                                return SourcePrefix.builder()
                                        .prefix(prefixVo.getPrefix())
                                        .order(order)
                                        .isTitle(prefixVo.getIsTitle())
                                        .build();
                            })
                            .toList();

                    return SourcePattern.builder()
                            .tokenSize(patternVo.getTokenSize())
                            .depth(depth)
                            .sourcePrefixes(sourcePrefixes)
                            .build();
                })
                .toList();

        // 전처리 중단 및 제외 패턴
        List<SourceStopPattern> sourceStopPatterns = command.getStopPatterns().stream()
                .map(prefix -> SourceStopPattern.builder()
                        .prefix(prefix)
                        .build())
                .toList();

        // 대상 문서 타입
        SourceType sourceType = SourceType.FILE;
        // 전처리 타입
        SelectType selectType = SelectType.valueOf(command.getSelectType().toUpperCase());
        // 추출 타입
        ExtractType extractType = ExtractType.valueOf(command.getExtractType().toUpperCase());

        // 파일 목록 생성
        UploadFile uploadFile = command.getFile();
        // 파일 생성
        FileDetail fileDetail = FileDetail.builder()
                .originFileName(uploadFile.getOriginFileName())
                .fileName(uploadFile.getFileName())
                .ip("127.0.0.1")
                .filePath(uploadFile.getFilePath())
                .fileSize(uploadFile.getFileSize())
                .ext(uploadFile.getExt())
                .url(uploadFile.getUrl())
                .sysCreateUser("SYSTEM")
                .sysModifyUser("SYSTEM")
                .build();

        // 문서 추출
        Document document = extractPort.extractFilePort(fileDetail, extractType.getCode());

        // 대상 문서 생성
        Source source = Source.builder()
                .version(-1L)
                .sourceType(sourceType)
                .selectType(selectType)
                .categoryCode("TRAIN-TEST-FILE")
                .name(StringUtil.removeExtension(fileDetail.getOriginFileName()))
                .content(document.getContent())
                .collectionId("COLLECTION-TEST-FILE")
                .fileDetailId(fileDetail.getFileDetailId())
                .maxTokenSize(command.getMaxTokenSize())
                .overlapSize(command.getOverlapSize())
                .isAuto(false)
                .sourcePatterns(sourcePatterns)
                .sourceStopPatterns(sourceStopPatterns)
                .build();

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(sourcePatterns)
                .stopPatterns(sourceStopPatterns)
                .selectType(selectType)
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> passages = SelectType.TOKEN.equals(selectType)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, command.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // 패시지 타이틀 지정
        for (Passage passage : passages) passage.update(source.getSourceId(), source.getVersion(), source.getName());

        // 청킹
        List<Chunk> chunks = new ArrayList<>();
        passages.forEach(passage -> chunks.addAll(passage.chunking(command.getMaxTokenSize(), command.getOverlapSize())));


        return ChunkResultVo.builder()
                .source(SourceVo.of(source))
                .passages(passages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    /**
     * 원격 문서 청킹
     *
     * @param command 원격 문서 청킹 Command
     */
    @Override
    public ChunkResultVo chunkRepoUseCase(ChunkRepoCommand command) {
        // 전처리 패턴
        List<SourcePattern> sourcePatterns = IntStream.range(0, command.getPatterns().size())
                .mapToObj(depth -> {
                    PatternVo patternVo = command.getPatterns().get(depth);
                    List<SourcePrefix> sourcePrefixes = IntStream.range(0, patternVo.getPrefixes().size())
                            .mapToObj(order -> {
                                PrefixVo prefixVo = patternVo.getPrefixes().get(order);

                                return SourcePrefix.builder()
                                        .prefix(prefixVo.getPrefix())
                                        .order(order)
                                        .isTitle(prefixVo.getIsTitle())
                                        .build();
                            })
                            .toList();

                    return SourcePattern.builder()
                            .tokenSize(patternVo.getTokenSize())
                            .depth(depth)
                            .sourcePrefixes(sourcePrefixes)
                            .build();
                })
                .toList();

        // 전처리 중단 및 제외 패턴
        List<SourceStopPattern> sourceStopPatterns = command.getStopPatterns().stream()
                .map(prefix -> SourceStopPattern.builder()
                        .prefix(prefix)
                        .build())
                .toList();

        // 대상 문서 타입
        SourceType sourceType = SourceType.REPO;
        // 전처리 타입
        SelectType selectType = SelectType.valueOf(command.getSelectType().toUpperCase());
        // 추출 타입
        ExtractType extractType = ExtractType.valueOf(command.getExtractType().toUpperCase());

        // 문서 추출
        Document document = documentReadPort.getRepoDocumentPort(command.getRepoType(), command.getRepoId(), extractType.getCode());

        // 대상 문서 생성
        Source source = Source.builder()
                .version(-1L)
                .sourceType(sourceType)
                .selectType(selectType)
                .categoryCode("TRAIN-REPO")
                .name(document.getName())
                .content(document.getContent())
                .collectionId("COLLECTION-REPO")
                .maxTokenSize(command.getMaxTokenSize())
                .overlapSize(command.getOverlapSize())
                .isAuto(false)
                .sourcePatterns(sourcePatterns)
                .sourceStopPatterns(sourceStopPatterns)
                .build();

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(sourcePatterns)
                .stopPatterns(sourceStopPatterns)
                .selectType(selectType)
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> passages = SelectType.TOKEN.equals(selectType)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, command.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // 패시지 타이틀 지정
        for (Passage passage : passages) passage.update(source.getSourceId(), source.getVersion(), source.getName());

        // 청킹
        List<Chunk> chunks = new ArrayList<>();
        passages.forEach(passage -> chunks.addAll(passage.chunking(command.getMaxTokenSize(), command.getOverlapSize())));

        return ChunkResultVo.builder()
                .source(SourceVo.of(source))
                .passages(passages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    /**
     * TODO: 청킹
     *
     * @param command 청킹 Command
     * @return 청킹 결과
     */
    @Override
    public ChunkResultVo chunkUseCase(ChunkCommand command) {
        return null;
    }

    /**
     * 청킹 배치
     *
     * @param command 청킹 배치 Command
     */
    @Transactional
    @Override
    public ChunkResultVo chunkBatchUseCase(ChunkBatchCommand command) {

        // 대상 문서 조회 (락)
        Source source = sourcePersistencePort.getSourcePortWithLock(command.getSourceId())
                .orElseThrow(NotFoundException::new);

        // 버전 업데이트
        source.increaseVersion();

        // 문서 메타 정보 조회
        FileDetail fileDetail = filePersistencePort.getFileDetailPort(source.getFileDetailId())
                .orElseThrow(NotFoundException::new);

        // 전처리 타입
        SelectType selectType = source.getSelectType();
        // 추출 타입
        ExtractType extractType = ExtractType.HTML;

        // 대상 문서 추출
        Document document;
        if (SourceType.FILE.equals(source.getSourceType())) {
            document = extractPort.extractFilePort(fileDetail, extractType.getCode());
        } else if (SourceType.REPO.equals(source.getSourceType())) {
            document = documentReadPort.getRepoDocumentPort(fileDetail.getUrl());
        } else throw new NotFoundException();

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(source.getSourcePatterns())
                .stopPatterns(source.getSourceStopPatterns())
                .selectType(selectType)
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> passages = SelectType.TOKEN.equals(selectType)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, source.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // 패시지 타이틀 지정
        for (Passage passage : passages) passage.update(source.getSourceId(), source.getVersion(), source.getName());

        // 패시지 영속화
        passages = sourcePersistenceAdapter.savePassagesPort(passages);

        // 청킹
        List<Chunk> chunks = new ArrayList<>();
        for (Passage passage : passages) {
            chunks.addAll(passage.chunking(source.getMaxTokenSize(), source.getOverlapSize()));
        }

        // 청킹 영속화
        chunks = sourcePersistenceAdapter.saveChunksPort(chunks);

        // 대상 문서 영속화
        source = sourcePersistencePort.saveSourcePort(source);

        return ChunkResultVo.builder()
                .source(SourceVo.of(source))
                .passages(passages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }
}