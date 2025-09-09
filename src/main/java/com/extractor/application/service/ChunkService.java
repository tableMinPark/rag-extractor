package com.extractor.application.service;

import com.extractor.application.port.DocumentPersistencePort;
import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.port.LawPersistencePort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.PassageVo;
import com.extractor.domain.model.FileDocument;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.extract.ExtractHwpxDocument;
import com.extractor.domain.model.extract.ExtractPassage;
import com.extractor.domain.model.extract.ExtractPdfDocument;
import com.extractor.domain.model.law.LawDocument;
import com.extractor.domain.model.law.LawPassage;
import com.extractor.domain.vo.document.FileDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<PassageVo> chunkHwpxDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractHwpxDocument extractHwpxDocument = extractPort.extractHwpxDocumentPort(fileDocument);
            extractHwpxDocument.extract();

            ExtractPassage extractPassage = new ExtractPassage(
                    extractHwpxDocument.getDocId(),
                    extractHwpxDocument.getExtractContents(),
                    chunkPatternVo.getPatterns(),
                    chunkPatternVo.getAntiPatterns());

            List<Passage> passages = extractPassage.chunk();

            // TODO: 원본 데이터 및 학습 데이터 DB 적재 (extractHwpxDocument, passages)

            return passages.stream()
                    .map(passage -> PassageVo.builder()
                            .docId(passage.getDocId())
                            .depth(passage.getDepth())
                            .tokenSize(passage.getTokenSize())
                            .fullTitle(passage.getFullTitle())
                            .titles(passage.getTitles())
                            .content(passage.getContent())
                            .subContent(passage.getSubContent())
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
    public List<PassageVo> chunkPdfDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            ExtractPdfDocument extractPdfDocument = extractPort.extractPdfDocumentPort(fileDocument);
            extractPdfDocument.extract();

            ExtractPassage extractPassage = new ExtractPassage(
                    extractPdfDocument.getDocId(),
                    extractPdfDocument.getExtractContents(),
                    chunkPatternVo.getPatterns(),
                    chunkPatternVo.getAntiPatterns());

            List<Passage> passages = extractPassage.chunk();

            // TODO: 원본 데이터 및 학습 데이터 DB 적재 (extractPdfDocument, passages)

            return passages.stream()
                    .map(passage -> PassageVo.builder()
                            .docId(passage.getDocId())
                            .depth(passage.getDepth())
                            .tokenSize(passage.getTokenSize())
                            .fullTitle(passage.getFullTitle())
                            .titles(passage.getTitles())
                            .content(passage.getContent())
                            .subContent(passage.getSubContent())
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
    @Transactional
    public List<PassageVo> chunkLawDocumentUseCase(Long lawId, ChunkPatternVo chunkPatternVo) {

        LawDocument lawDocument = lawPersistencePort.getLawDocumentsPort(lawId);

        LawPassage lawPassage = new LawPassage(
                String.valueOf(lawDocument.getLawId()),
                lawDocument.getLawContents(),
                lawDocument.getLawLinks(),
                chunkPatternVo.getPatterns(),
                chunkPatternVo.getAntiPatterns());

        List<Passage> passages = lawPassage.chunk();

        // TODO: 원본 데이터 및 학습 데이터 DB 적재 (lawDocument, passages)

        return passages.stream()
                .map(passage -> PassageVo.builder()
                        .docId(passage.getDocId())
                        .depth(passage.getDepth())
                        .tokenSize(passage.getTokenSize())
                        .fullTitle(passage.getFullTitle())
                        .titles(passage.getTitles())
                        .content(passage.getContent())
                        .subContent(passage.getSubContent())
                        .build())
                .toList();
    }
}
