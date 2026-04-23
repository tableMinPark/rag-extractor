package com.genai.extractor.application.service;

import com.genai.extractor.adapter.out.constant.FileConst;
import com.genai.extractor.application.command.CreateSourceCommand;
import com.genai.extractor.application.command.GetSourceCommand;
import com.genai.extractor.application.command.GetSourcesCommand;
import com.genai.extractor.application.enums.SelectType;
import com.genai.extractor.application.enums.SourceType;
import com.genai.extractor.application.port.CommonCodePersistencePort;
import com.genai.extractor.application.port.ExtractPort;
import com.genai.extractor.application.port.FilePersistencePort;
import com.genai.extractor.application.port.SourcePersistencePort;
import com.genai.extractor.application.usecase.SourceUseCase;
import com.genai.extractor.application.vo.CommonCodeVo;
import com.genai.extractor.application.vo.SourceVo;
import com.genai.global.wrapper.PageWrapper;
import com.genai.extractor.domain.model.*;
import com.genai.extractor.domain.vo.PatternVo;
import com.genai.extractor.domain.vo.PrefixVo;
import com.genai.common.utils.StringUtil;
import com.genai.common.vo.UploadFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SourceService implements SourceUseCase {

    private final SourcePersistencePort sourcePersistencePort;
    private final FilePersistencePort filePersistencePort;
    private final CommonCodePersistencePort commonCodePersistencePort;
    private final ExtractPort extractPort;

    /**
     * 대상 문서 등록
     *
     * @param command 대상 문서 등록 Command
     */
    @Transactional
    @Override
    public void createSourcesUseCase(CreateSourceCommand command) {

        // 대상 문서 타입
        SourceType sourceType = SourceType.valueOf(command.getSourceType().toUpperCase());
        // 전처리 타입
        SelectType selectType = SelectType.valueOf(command.getSelectType().toUpperCase());

        // 파일 목록 생성
        UploadFile uploadFile = command.getFile();

        // 파일 정보 등록
        FileDetail fileDetail = filePersistencePort.saveFileDetailPort(FileDetail.builder()
                .originFileName(uploadFile.getOriginFileName())
                .fileName(uploadFile.getFileName())
                .ip(uploadFile.getIp())
                .filePath(uploadFile.getFilePath())
                .fileSize(uploadFile.getFileSize())
                .ext(uploadFile.getExt())
                .url(uploadFile.getUrl())
                .sysCreateUser(FileConst.FILE_PERSIST_USER)
                .sysModifyUser(FileConst.FILE_PERSIST_USER)
                .build());

        String content = "";

        // 물리 파일인 경우 텍스트 추출
        if (SourceType.FILE.equals(sourceType)) {
            content = extractPort.extractTextPort(fileDetail);
        }

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

        // 대상 문서 생성
        Source source = Source.builder()
                .sourceType(sourceType)
                .selectType(selectType)
                .version(0L)
                .categoryCode(command.getCategoryCode())
                .name(StringUtil.removeExtension(fileDetail.getOriginFileName()))
                .content(content)
                .collectionId(command.getCollectionId())
                .fileDetailId(fileDetail.getFileDetailId())
                .maxTokenSize(command.getMaxTokenSize())
                .overlapSize(command.getOverlapSize())
                .isAuto(command.getIsAuto())
                .isBatch(false)
                .sourcePatterns(sourcePatterns)
                .sourceStopPatterns(sourceStopPatterns)
                .build();

        // 영속화
        sourcePersistencePort.saveSourcePort(source);
    }

    /**
     * 배치 대상 문서 목록 조회
     *
     * @return 배치 대상 문서 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<SourceVo> getActiveSourcesUseCase() {
        return sourcePersistencePort.getActiveSourcesPort().stream()
                .map(SourceVo::of)
                .toList();
    }

    /**
     * 대상 문서 조회
     *
     * @param command 대상 문서 조회 Command
     * @return 대상 문서
     */
    @Transactional(readOnly = true)
    @Override
    public SourceVo getSourceUseCase(GetSourceCommand command) {
        return SourceVo.of(sourcePersistencePort.getSourcePort(command.getSourceId()));
    }

    /**
     * 대상 문서 목록 조회
     *
     * @param command 대상 문서 목록 조회 Command
     * @return 대상 문서 목록
     */
    @Transactional(readOnly = true)
    @Override
    public PageWrapper<SourceVo> getSourcesUseCase(GetSourcesCommand command) {

        PageWrapper<Source> sourcePageWrapper = sourcePersistencePort.getSourcesPort(
                command.getPage(), command.getSize(), command.getOrderBy(), command.getOrder(),
                command.getKeyword(), command.getCategoryCode());

        return PageWrapper.<SourceVo>builder()
                .content(sourcePageWrapper.getContent().stream().map(SourceVo::of).toList())
                .isLast(sourcePageWrapper.isLast())
                .pageNo(sourcePageWrapper.getPageNo())
                .pageSize(sourcePageWrapper.getPageSize())
                .totalCount(sourcePageWrapper.getTotalCount())
                .totalPages(sourcePageWrapper.getTotalPages())
                .build();
    }

    /**
     * 대상 문서 카테고리 목록 조회
     *
     * @return 대상 문서 카테고리 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<CommonCodeVo> getCategoriesSourceUseCase() {
        return commonCodePersistencePort.getComnCodesByCodeGroupPort("TRAIN").stream()
                .map(CommonCodeVo::of)
                .toList();
    }

    /**
     * 총 대상 문서 수 조회
     *
     * @return 총 대상 문서 수
     */
    @Transactional(readOnly = true)
    @Override
    public long getSourceTotalCountUseCase() {
        return sourcePersistencePort.getSourceTotalCountPort();
    }

    /**
     * 대상 문서 삭제
     *
     * @param sourceId 대상 문서 ID
     */
    @Transactional
    @Override
    public void deleteSourceUseCase(Long sourceId) {
        sourcePersistencePort.deleteSourcePort(sourceId);
    }

    /**
     * 대상 문서 배치 여부 수정
     *
     * @param sourceId 대상 문서 ID
     * @param isBatch  배치 여부
     */
    @Transactional
    @Override
    public void updateIsBatchUseCase(Long sourceId, boolean isBatch) {
        sourcePersistencePort.updateIsBatchPort(sourceId, isBatch);
    }
}