package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.port.LawPersistencePort;
import com.extractor.application.port.ManualPersistencePort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.ChunkDocumentVo;
import com.extractor.application.vo.OriginalDocumentVo;
import com.extractor.application.vo.TrainingDocumentVo;
import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.ChunkOption;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDocument;
import com.extractor.application.vo.ChunkPatternVo;
import com.extractor.application.vo.FileDocumentVo;
import com.extractor.global.enums.DocumentType;
import com.extractor.global.enums.ExtractType;
import com.extractor.global.enums.FileExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkService implements ChunkUseCase {

    private final ExtractPort extractPort;

    private final FilePort filePort;

    private final LawPersistencePort lawPersistencePort;

    private final ManualPersistencePort manualPersistencePort;

    /**
     * 한글 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     * @param extractType    표 데이터 변환 타입
     */
    @Override
    public ChunkDocumentVo chunkHwpxDocumentUseCase(String version, String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo, ExtractType extractType) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        Document document;
        if (FileExtension.PDF.equals(fileDocument.getExtension())) {
            document = extractPort.extractPdfDocumentPort(fileDocument);
        } else {
            document = extractPort.extractHwpxDocumentPort(fileDocument, extractType);
        }

        try {
            OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                    .version(version)
                    .docType(DocumentType.FILE.getCode())
                    .categoryCode(categoryCode)
                    .name(document.getName())
                    .content(document.getContent())
                    .build();

            List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                    originalDocumentVo.getName(),
                    originalDocumentVo.getDocType(),
                    originalDocumentVo.getCategoryCode(),
                    originalDocumentVo.getVersion(),
                    Chunk.chunking(document.getDocumentContents(), ChunkOption.builder()
                            .maxTokenSize(chunkPatternVo.getMaxTokenSize())
                            .overlapSize(chunkPatternVo.getOverlapSize())
                            .patterns(chunkPatternVo.getPatterns())
                            .type(ChunkOption.ChunkType.REGEX)
                            .build()));

            return new ChunkDocumentVo(originalDocumentVo, trainingDocumentVos);

        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * PDF 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public ChunkDocumentVo chunkPdfDocumentUseCase(String version, String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            Document document = extractPort.extractPdfDocumentPort(fileDocument);

            OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                    .version(version)
                    .docType(DocumentType.FILE.getCode())
                    .categoryCode(categoryCode)
                    .name(document.getName())
                    .content(document.getContent())
                    .build();

            List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                    originalDocumentVo.getName(),
                    originalDocumentVo.getDocType(),
                    originalDocumentVo.getCategoryCode(),
                    originalDocumentVo.getVersion(),
                    Chunk.chunking(document.getDocumentContents(), ChunkOption.builder()
                            .maxTokenSize(chunkPatternVo.getMaxTokenSize())
                            .overlapSize(chunkPatternVo.getOverlapSize())
                            .patterns(chunkPatternVo.getPatterns())
                            .type(ChunkOption.ChunkType.REGEX)
                            .build()));

            return new ChunkDocumentVo(originalDocumentVo, trainingDocumentVos);

        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * 법령 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param lawId          법령 목록
     * @param chunkPatternVo 청킹 패턴 정보
     */
    @Override
    @Transactional
    public ChunkDocumentVo chunkLawDocumentUseCase(String version, String categoryCode, Long lawId, ChunkPatternVo chunkPatternVo) {

        Document document = lawPersistencePort.getLawDocumentsPort(lawId);

        OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                .version(version)
                .docType(DocumentType.DB.getCode())
                .categoryCode(categoryCode)
                .name(document.getName())
                .content(document.getContent())
                .build();

        List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                originalDocumentVo.getName(),
                originalDocumentVo.getDocType(),
                originalDocumentVo.getCategoryCode(),
                originalDocumentVo.getVersion(),
                Chunk.chunking(document.getDocumentContents(), ChunkOption.builder()
                        .maxTokenSize(chunkPatternVo.getMaxTokenSize())
                        .overlapSize(chunkPatternVo.getOverlapSize())
                        .patterns(chunkPatternVo.getPatterns())
                        .type(ChunkOption.ChunkType.EQUALS)
                        .build()));

        return new ChunkDocumentVo(originalDocumentVo, trainingDocumentVos);
    }

    /**
     * 메뉴얼 문서 청킹
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     */
    @Override
    public ChunkDocumentVo chunkManualDocumentUseCase(String version, String categoryCode, Long manualId) {

        Document document = manualPersistencePort.getManualDocumentsPort(manualId);

        OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                .version(version)
                .docType(DocumentType.DB.getCode())
                .categoryCode(categoryCode)
                .name(document.getName())
                .content(document.getContent())
                .build();

        List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                originalDocumentVo.getName(),
                originalDocumentVo.getDocType(),
                originalDocumentVo.getCategoryCode(),
                originalDocumentVo.getVersion(),
                Chunk.chunking(document.getDocumentContents(), ChunkOption.builder()
                        .maxTokenSize(Integer.MIN_VALUE)
                        .overlapSize(0)
                        .patterns(Collections.emptyList())
                        .type(ChunkOption.ChunkType.NONE)
                        .build()));

        return new ChunkDocumentVo(originalDocumentVo, trainingDocumentVos);
    }

    /**
     * Chunk Vo -> 전처리 문서로 변환
     */
    private static List<TrainingDocumentVo> chunkToTrainingDocument(String title, String docType, String categoryCode, String version, List<Chunk> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    String subTitle = chunk.getDocumentContents().size() == 1 ? chunk.getDocumentContents().getFirst().getTitle() : "";
                    String thirdTitle = "";
                    String content = chunk.getContent();
                    String subContent = chunk.getSubContent();
                    int contentTokenSize = chunk.getContentTokenSize();
                    int subContentTokenSize = chunk.getSubContentTokenSize();
                    int totalTokenSize = chunk.getTotalTokenSize();

                    String[] titles = chunk.getTitles();

                    if (titles.length > 0) {
                        subTitle = titles[0];
                    }

                    if (titles.length > 1) {
                        thirdTitle = String.join(" | ", Arrays.stream(titles)
                                        .toList()
                                        .subList(1, titles.length)
                                        .stream()
                                        .filter(s -> !s.isBlank()).toList())
                                .trim();
                    }

                    return TrainingDocumentVo.builder()
                            .docType(docType)
                            .categoryCode(categoryCode)
                            .version(version)
                            .title(title)
                            .subTitle(subTitle)
                            .thirdTitle(thirdTitle)
                            .content(content)
                            .subContent(subContent)
                            .totalTokenSize(totalTokenSize)
                            .contentTokenSize(contentTokenSize)
                            .subContentTokenSize(subContentTokenSize)
                            .build();
                })
                .toList();
    }
}