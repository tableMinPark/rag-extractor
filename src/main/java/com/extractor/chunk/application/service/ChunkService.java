package com.extractor.chunk.application.service;

import com.extractor.chunk.application.port.DocumentPersistencePort;
import com.extractor.chunk.application.port.ExtractPort;
import com.extractor.chunk.application.port.FilePort;
import com.extractor.chunk.application.port.LawPersistencePort;
import com.extractor.chunk.application.usecase.ChunkUseCase;
import com.extractor.chunk.application.vo.ChunkVo;
import com.extractor.chunk.domain.model.*;
import com.extractor.chunk.domain.vo.ChunkPatternVo;
import com.extractor.extract.domain.model.ExtractChunk;
import com.extractor.extract.domain.model.ExtractHwpxDocument;
import com.extractor.extract.domain.model.ExtractPdfDocument;
import com.extractor.extract.domain.model.FileDocument;
import com.extractor.extract.domain.vo.FileDocumentVo;
import com.extractor.global.utils.StringUtil;
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

    private final DocumentPersistencePort documentPersistencePort;

    /**
     * 한글 문서 청킹
     *
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    @Override
    public List<ChunkVo> chunkHwpxDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractHwpxDocument extractHwpxDocument = extractPort.extractHwpxDocumentPort(fileDocument);
            extractHwpxDocument.extract();

            ExtractChunk extractChunk = new ExtractChunk(
                    extractHwpxDocument.getExtractContents(),
                    chunkPatternVo.getPatterns(),
                    chunkPatternVo.getAntiPatterns());

            List<Chunk> chunks = extractChunk.chunking();

            // TODO: 원본 데이터 및 학습 데이터 DB 적재 (extractHwpxDocument, chunks)

            return chunks.stream()
                    .map(chunk -> ChunkVo.builder()
                            .depth(chunk.getDepth())
                            .tokenSize(chunk.getTokenSize())
                            .fullTitle(chunk.getFullTitle())
                            .titles(chunk.getTitles())
                            .content(chunk.getContent())
                            .subContent(chunk.getSubContent())
                            .build())
                    .toList();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * PDF 문서 청킹
     *
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public List<ChunkVo> chunkPdfDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractPdfDocument extractPdfDocument = extractPort.extractPdfDocumentPort(fileDocument);
            extractPdfDocument.extract();

            ExtractChunk extractChunk = new ExtractChunk(
                    extractPdfDocument.getExtractContents(),
                    chunkPatternVo.getPatterns(),
                    chunkPatternVo.getAntiPatterns());

            List<Chunk> chunks = extractChunk.chunking();

            // TODO: 원본 데이터 및 학습 데이터 DB 적재 (extractPdfDocument, chunks)

            return chunks.stream()
                    .map(chunk -> ChunkVo.builder()
                            .depth(chunk.getDepth())
                            .tokenSize(chunk.getTokenSize())
                            .fullTitle(chunk.getFullTitle())
                            .titles(chunk.getTitles())
                            .content(chunk.getContent())
                            .subContent(chunk.getSubContent())
                            .build())
                    .toList();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * 법령 문서 청킹
     *
     * @param lawId          법령 목록
     * @param chunkPatternVo 청킹 패턴 정보
     */
    @Override
    @Transactional
    public List<ChunkVo> chunkLawDocumentUseCase(Long lawId, ChunkPatternVo chunkPatternVo) {

        String docType = "DOC-TYPE-DB";
        String categoryCode = "TRAIN-LAW";

        LawDocument lawDocument = lawPersistencePort.getLawDocumentsPort(lawId);

        LawChunk lawChunk = new LawChunk(
                lawDocument.getLawContents(),
                lawDocument.getLawLinks(),
                chunkPatternVo.getPatterns(),
                chunkPatternVo.getAntiPatterns());

        // 원본 문서 영속화
        OriginalDocument originalDocument = documentPersistencePort.saveOriginalDocumentPort(OriginalDocument.builder()
                .version(StringUtil.generateRandomId())
                .docType(docType)
                .categoryCode(categoryCode)
                .name(lawDocument.getLawName())
                .content(lawDocument.getContent())
                .build());

        return lawChunk.chunking().stream()
                .map(chunk -> {
                    String title = lawDocument.getLawName();
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

                    documentPersistencePort.saveTrainingDocumentPort(TrainingDocument.builder()
                            .originalId(originalDocument.getOriginalId())
                            .docType(docType)
                            .categoryCode(categoryCode)
                            .version(originalDocument.getVersion())
                            .title(title)
                            .subTitle(subTitle)
                            .thirdTitle(thirdTitle)
                            .content(content)
                            .subContent(subContent)
                            .tokenSize(tokenSize)
                            .build());

                    return ChunkVo.builder()
                            .depth(chunk.getDepth())
                            .tokenSize(chunk.getTokenSize())
                            .fullTitle(chunk.getFullTitle())
                            .titles(chunk.getTitles())
                            .content(chunk.getContent())
                            .subContent(chunk.getSubContent())
                            .build();
                })
                .toList();
    }
}