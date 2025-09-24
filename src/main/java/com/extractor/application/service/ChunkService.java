package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.port.LawPersistencePort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.ChunkDocumentVo;
import com.extractor.application.vo.OriginalDocumentVo;
import com.extractor.application.vo.TrainingDocumentVo;
import com.extractor.domain.model.*;
import com.extractor.domain.vo.ChunkPatternVo;
import com.extractor.domain.vo.FileDocumentVo;
import com.extractor.global.enums.FileExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkService implements ChunkUseCase {

    private final ExtractPort extractPort;

    private final FilePort filePort;

    private final LawPersistencePort lawPersistencePort;

    /**
     * 한글 문서 청킹
     *
     * @param version        버전 구분 코드
     * @param categoryCode   카테고리 코드
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    @Override
    public ChunkDocumentVo chunkHwpxDocumentUseCase(String version, String categoryCode, FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        String docType = "DOC-TYPE-FILE";

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        ExtractDocument extractDocument;
        if (FileExtension.PDF.equals(fileDocument.getExtension())) {
            ExtractPdfDocument extractPdfDocument = extractPort.extractPdfDocumentPort(fileDocument);
            extractPdfDocument.extract();
            extractDocument = extractPdfDocument;
        } else {
            ExtractHwpxDocument extractHwpxDocument = extractPort.extractHwpxDocumentPort(fileDocument);
            extractHwpxDocument.extract();
            extractDocument = extractHwpxDocument;
        }

        try {
            ExtractChunk extractChunk = new ExtractChunk(
                    extractDocument.getExtractContents(),
                    chunkPatternVo.getPatterns(),
                    chunkPatternVo.getAntiPatterns(),
                    chunkPatternVo.getMaxTokenSize());

            StringBuilder contentBuilder = new StringBuilder();
            extractDocument.getExtractContents().forEach(extractContent -> {
                contentBuilder.append(extractContent.getContent()).append("\n");
            });


            OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                    .version(version)
                    .docType(docType)
                    .categoryCode(categoryCode)
                    .name(extractDocument.getName())
                    .content(contentBuilder.toString().trim())
                    .build();

            List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                    originalDocumentVo.getName(),
                    originalDocumentVo.getDocType(),
                    originalDocumentVo.getCategoryCode(),
                    originalDocumentVo.getVersion(),
                    extractChunk.chunking());

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

        String docType = "DOC-TYPE-FILE";

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractPdfDocument extractDocument = extractPort.extractPdfDocumentPort(fileDocument);
            extractDocument.extract();

            ExtractChunk extractChunk = new ExtractChunk(
                    extractDocument.getExtractContents(),
                    chunkPatternVo.getPatterns(),
                    chunkPatternVo.getAntiPatterns(),
                    chunkPatternVo.getMaxTokenSize());

            OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                    .version(version)
                    .docType(docType)
                    .categoryCode(categoryCode)
                    .name(extractDocument.getName())
                    .content(extractDocument.getContent())
                    .build();

            List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                    originalDocumentVo.getName(),
                    originalDocumentVo.getDocType(),
                    originalDocumentVo.getCategoryCode(),
                    originalDocumentVo.getVersion(),
                    extractChunk.chunking());

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

        String docType = "DOC-TYPE-DB";

        LawDocument lawDocument = lawPersistencePort.getLawDocumentsPort(lawId);

        LawChunk lawChunk = new LawChunk(
                lawDocument.getLawContents(),
                lawDocument.getLawLinks(),
                chunkPatternVo.getPatterns(),
                chunkPatternVo.getAntiPatterns(),
                chunkPatternVo.getMaxTokenSize());

        OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                .version(version)
                .docType(docType)
                .categoryCode(categoryCode)
                .name(lawDocument.getLawName())
                .content(lawDocument.getContent())
                .build();

        List<TrainingDocumentVo> trainingDocumentVos = chunkToTrainingDocument(
                originalDocumentVo.getName(),
                originalDocumentVo.getDocType(),
                originalDocumentVo.getCategoryCode(),
                originalDocumentVo.getVersion(),
                lawChunk.chunking());

        return new ChunkDocumentVo(originalDocumentVo, trainingDocumentVos);
    }

    /**
     * Chunk Vo -> 전처리 문서로 변환
     */
    private static List<TrainingDocumentVo> chunkToTrainingDocument(String title, String docType, String categoryCode, String version, List<Chunk> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    String subTitle = "";
                    String thirdTitle = "";
                    String content = chunk.getContent();
                    String subContent = chunk.getSubContent();
                    int tokenSize = chunk.getTokenSize();

                    String[] titles = chunk.getTitles();
                    if (titles.length > 0) {
                        subTitle = titles[0];
                    }
                    if (titles.length > 1) {
                        thirdTitle = String.join(Chunk.TITLE_PREFIX, Arrays.stream(titles)
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
                            .tokenSize(tokenSize)
                            .build();
                })
                .toList();
    }
}