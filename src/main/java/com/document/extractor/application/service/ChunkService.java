package com.document.extractor.application.service;

import com.document.extractor.adapter.out.constant.FileConst;
import com.document.extractor.application.command.*;
import com.document.extractor.application.enums.ExtractType;
import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.application.enums.UpdateState;
import com.document.extractor.application.exception.InvalidSourceTypeException;
import com.document.extractor.application.port.*;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.vo.ChunkResultVo;
import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.vo.SourceVo;
import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.factory.PassageFactory;
import com.document.extractor.domain.model.*;
import com.document.extractor.domain.vo.PassageOptionVo;
import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.domain.vo.PrefixVo;
import com.document.global.vo.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkService implements ChunkUseCase {

    private final ExtractPort extractPort;
    private final DocumentReadPort documentReadPort;
    private final FilePersistencePort filePersistencePort;
    private final SourcePersistencePort sourcePersistencePort;
    private final PassagePersistencePort passagePersistencePort;

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     * @return 청킹 결과
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
                .sysCreateUser(FileConst.FILE_PERSIST_USER)
                .sysModifyUser(FileConst.FILE_PERSIST_USER)
                .build();

        // 문서 추출
        Document document = extractPort.extractFilePort(fileDetail, extractType.getCode());

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(sourcePatterns)
                .stopPatterns(sourceStopPatterns)
                .selectType(selectType)
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> currentPassages = SelectType.TOKEN.equals(selectType)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, command.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // Passage 목록 Source 연결
        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            // 대상 문서 정보 동기화
            currentPassage.connectSource(null, document.getName());
            // 정렬 필드 및 버전 저장
            currentPassage.update(null, sortOrder);
        }

        // 청킹
        List<Chunk> chunks = new ArrayList<>();
        currentPassages.forEach(passage -> chunks.addAll(passage.chunking(command.getMaxTokenSize(), command.getOverlapSize())));

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .previousPassages(Collections.emptyList())
                .currentPassages(currentPassages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    /**
     * 원격 문서 청킹
     *
     * @param command 원격 문서 청킹 Command
     * @return 청킹 결과
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

        // 전처리 타입
        SelectType selectType = SelectType.valueOf(command.getSelectType().toUpperCase());
        // 추출 타입
        ExtractType extractType = ExtractType.valueOf(command.getExtractType().toUpperCase());

        // 문서 추출
        Document document = documentReadPort.getRepoDocumentPort(command.getUri(), extractType.getCode());

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(sourcePatterns)
                .stopPatterns(sourceStopPatterns)
                .selectType(selectType)
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> currentPassages = SelectType.TOKEN.equals(selectType)
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, command.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // Passage 목록 Source 연결
        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            // 대상 문서 정보 동기화
            currentPassage.connectSource(null, document.getName());
            // 정렬 필드 및 버전 저장
            currentPassage.update(null, sortOrder);
        }

        // 청킹
        List<Chunk> chunks = new ArrayList<>();
        currentPassages.forEach(passage -> chunks.addAll(passage.chunking(command.getMaxTokenSize(), command.getOverlapSize())));

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .previousPassages(Collections.emptyList())
                .currentPassages(currentPassages.stream().map(PassageVo::of).toList())
                .chunks(chunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    /**
     * 대상 문서 청킹
     *
     * @param command 대상 문서 청킹 Command
     * @return 청킹 결과
     */
    @Transactional(readOnly = true)
    @Override
    public ChunkResultVo chunkSourceUseCase(ChunkSourceCommand command) {
        // 대상 문서 조회
        Source source = sourcePersistencePort.getSourcePort(command.getSourceId());

        // 버전 변경
        source.nextVersion();

        // 문서 메타 정보 조회
        FileDetail fileDetail = filePersistencePort.getFileDetailPort(source.getFileDetailId());

        // 대상 문서 추출
        Document document;
        if (SourceType.FILE.equals(source.getSourceType())) {
            document = extractPort.extractFilePort(fileDetail, ExtractType.HTML.getCode());
        } else if (SourceType.REPO.equals(source.getSourceType())) {
            document = documentReadPort.getRepoDocumentPort(fileDetail.getUrl(), ExtractType.HTML.getCode());
        } else throw new InvalidSourceTypeException();

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(source.getSourcePatterns())
                .stopPatterns(source.getSourceStopPatterns())
                .selectType(source.getSelectType())
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> currentPassages = SelectType.TOKEN.equals(source.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, source.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // Passage 목록 Source 연결
        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            // 대상 문서 정보 동기화
            currentPassage.connectSource(source.getSourceId(), source.getName());
            // 정렬 필드 및 버전 저장
            currentPassage.update(source.getVersion(), sortOrder);
        }

        // 이전 버전 패시지 조회
        List<Passage> previousPassages = passagePersistencePort.getPassagesByVersion(source.getSourceId(), source.getPreviousVersion());

        // 패시지 버전 비교
        Passage.compareDiff(previousPassages, currentPassages);

        // 패시지 정렬 필드 매핑
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

        // 최초 버전인 경우 청크 생성
        List<Chunk> chunks = new ArrayList<>();
        if (source.isFirstVersion()) {
            for (Passage passage : currentPassages) {
                // 새로운 청크 생성
                chunks.addAll(passage.chunking(source.getMaxTokenSize(), source.getOverlapSize()));
            }
        }
        // 최초 버전이 아닌 경우
        else {
            for (Passage passage : currentPassages) {
                // 기존 청크 재매핑 (청크 <-> 패시지 매핑 변경)
                switch (passage.getUpdateState()) {
                    case CHANGE, STAY -> {
                        if (passage.getParentSortOrder() != null) {
                            chunks.addAll(passagePersistencePort.getChunkBySortOrderAndVersion(passage.getSourceId(), passage.getParentSortOrder(), source.getPreviousVersion()).stream()
                                    .peek(chunk -> chunk.update(passage.getPassageId(), passage.getVersion()))
                                    .toList());
                        }
                    }
                    // 새로운 청크 생성
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

    /**
     * 청킹 배치
     *
     * @param command 청킹 배치 Command
     */
    @Transactional
    @Override
    public ChunkResultVo chunkBatchUseCase(ChunkBatchCommand command) {
        // 대상 문서 조회 (락)
        Source source = sourcePersistencePort.getSourceWithLockPort(command.getSourceId());

        // 버전 변경
        source.nextVersion();

        // 문서 메타 정보 조회
        FileDetail fileDetail = filePersistencePort.getFileDetailPort(source.getFileDetailId());

        // 대상 문서 추출
        Document document;
        if (SourceType.FILE.equals(source.getSourceType())) {
            document = extractPort.extractFilePort(fileDetail, ExtractType.HTML.getCode());
        } else if (SourceType.REPO.equals(source.getSourceType())) {
            document = documentReadPort.getRepoDocumentPort(fileDetail.getUrl(), ExtractType.HTML.getCode());
        } else throw new InvalidSourceTypeException();

        // 패시징 옵션
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(source.getSourcePatterns())
                .stopPatterns(source.getSourceStopPatterns())
                .selectType(source.getSelectType())
                .build();

        // 패시징 (토큰 or 패턴)
        List<Passage> currentPassages = SelectType.TOKEN.equals(source.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, source.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        // Passage 목록 Source 연결
        for (int sortOrder = 0; sortOrder < currentPassages.size(); sortOrder++) {
            Passage currentPassage = currentPassages.get(sortOrder);
            // 대상 문서 정보 동기화
            currentPassage.connectSource(source.getSourceId(), source.getName());
            // 정렬 필드 및 버전 저장
            currentPassage.update(source.getVersion(), sortOrder);
        }

        // 이전 버전 패시지 조회
        List<Passage> previousPassages = passagePersistencePort.getPassagesByVersion(source.getSourceId(), source.getPreviousVersion());

        // 패시지 버전 비교
        Passage.compareDiff(previousPassages, currentPassages);

        // 패시지 정렬 필드 매핑
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

        // 대상 문서 영속화
        Source persistSource = sourcePersistencePort.saveSourcePort(source);
        // 이전 패시지 영속화 (이력 코드 변경)
        List<Passage> persistPreviousPassages = passagePersistencePort.savePassagesPort(previousPassages);
        // 패시지 영속화
        List<Passage> persistCurrentPassages = passagePersistencePort.savePassagesPort(currentPassages);

        // 최초 버전인 경우 청크 생성
        List<Chunk> chunks = new ArrayList<>();
        if (persistSource.isFirstVersion()) {
            for (Passage passage : persistCurrentPassages) {
                // 새로운 청크 생성
                chunks.addAll(passage.chunking(persistSource.getMaxTokenSize(), persistSource.getOverlapSize()));
            }
        }
        // 최초 버전이 아닌 경우
        else {
            for (Passage passage : persistCurrentPassages) {
                switch (passage.getUpdateState()) {
                    //  기존 청크 재매핑 (청크 <-> 패시지 매핑 변경)
                    case CHANGE, STAY -> {
                        if (passage.getParentSortOrder() != null) {
                            chunks.addAll(passagePersistencePort.getChunkBySortOrderAndVersion(passage.getSourceId(), passage.getParentSortOrder(), persistSource.getPreviousVersion()).stream()
                                    .peek(chunk -> chunk.update(passage.getPassageId(), passage.getVersion()))
                                    .toList());
                        }
                    }
                    // 새로운 청크 생성
                    case INSERT ->
                            chunks.addAll(passage.chunking(persistSource.getMaxTokenSize(), persistSource.getOverlapSize()));
                }
            }
        }
        // 청크 영속화
        List<Chunk> persistChunks = passagePersistencePort.saveChunksPort(chunks);

        return ChunkResultVo.builder()
                .isConvertError(document.getConvertError())
                .source(SourceVo.of(persistSource))
                .previousPassages(persistPreviousPassages.stream().map(PassageVo::of).toList())
                .currentPassages(persistCurrentPassages.stream().map(PassageVo::of).toList())
                .chunks(persistChunks.stream().map(ChunkVo::of).toList())
                .build();
    }

    /**
     * TODO: 청크 등록
     *
     * @param command 청크 등록 Command
     */
    @Transactional
    @Override
    public void createChunkUseCase(CreateChunkCommand command) {

    }

    /**
     * TODO: 청크 조회
     *
     * @param command 청크 조회 Command
     * @return 청크
     */
    @Transactional(readOnly = true)
    @Override
    public ChunkVo getChunkUseCase(GetChunkCommand command) {
        return null;
    }

    /**
     * TODO: 청크 목록 조회
     *
     * @param command 청크 목록 조회 Command
     * @return 청크 목록
     */
    @Transactional(readOnly = true)
    @Override
    public PageWrapper<ChunkVo> getChunksUseCase(GetChunksCommand command) {
        return PageWrapper.<ChunkVo>builder()
                .build();
    }

    /**
     * TODO: 청크 수정
     *
     * @param command 청크 수정 Command
     */
    @Transactional
    @Override
    public void updateChunkUseCase(UpdateChunkCommand command) {

    }

    /**
     * TODO: 청크 삭제
     *
     * @param command 청크 삭제 Command
     */
    @Transactional
    @Override
    public void deleteChunkUseCase(DeleteChunkCommand command) {

    }

    /**
     * TODO: 패시지 조회
     *
     * @param command 패시지 조회 Command
     * @return 패시지
     */
    @Transactional(readOnly = true)
    @Override
    public PassageVo getPassageUseCase(GetPassageCommand command) {
        return null;
    }

    /**
     * TODO: 패시지 목록 조회
     *
     * @param command 패시지 목록 조회 Command
     * @return 패시지 목록
     */
    @Transactional(readOnly = true)
    @Override
    public PageWrapper<PassageVo> getPassagesUseCase(GetPassagesCommand command) {
        return PageWrapper.<PassageVo>builder()
                .build();
    }
}