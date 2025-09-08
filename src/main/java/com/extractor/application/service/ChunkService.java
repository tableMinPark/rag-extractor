package com.extractor.application.service;

import com.extractor.application.port.DocumentPersistencePort;
import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.port.LawPersistencePort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.PassageDocumentVo;
import com.extractor.domain.model.*;
import com.extractor.domain.model.law.LawContent;
import com.extractor.domain.model.law.LawDocument;
import com.extractor.domain.model.law.LawLink;
import com.extractor.domain.model.pattern.ExtractDocument;
import com.extractor.domain.model.pattern.HwpxDocument;
import com.extractor.domain.model.pattern.PatternPassageDocument;
import com.extractor.domain.model.pattern.PdfDocument;
import com.extractor.domain.vo.document.FileDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     * 문서 청킹
     * @param extractDocument 추출 문서
     * @param chunkPatternVo 청킹 패턴
     */
    private List<PatternPassageDocument> chunkDocumentUseCase(ExtractDocument extractDocument, ChunkPatternVo chunkPatternVo) {

        PatternPassageDocument patternPassageDocument = new PatternPassageDocument(
                extractDocument.getDocId(),
                chunkPatternVo.getPatterns().size(),
                extractDocument.getLines());

        return patternPassageDocument.chunk(
                chunkPatternVo.getPatterns(), chunkPatternVo.getStopPatterns());
    }

    /**
     * 한글 문서 청킹
     * @param fileDocumentVo 원본 문서 정보
     * @param chunkPatternVo 청킹 패턴 정보
     */
    @Override
    public List<PassageDocumentVo> chunkHwpxDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(fileDocument);
            hwpxDocument.extract();
            return this.chunkDocumentUseCase(hwpxDocument, chunkPatternVo).stream()
                    .map(patternPassageDocument -> PassageDocumentVo.builder()
                            .docId(patternPassageDocument.getDocId())
                            .depth(patternPassageDocument.getDepth())
                            .tokenSize(patternPassageDocument.getTokenSize())
                            .fullTitle(patternPassageDocument.getFullTitle())
                            .titles(patternPassageDocument.getTitles())
                            .content(patternPassageDocument.getContent())
                            .build())
                    .toList();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * PDF 문서 청킹
     * @param fileDocumentVo 원본 문서 정보
     */
    @Override
    public List<PassageDocumentVo> chunkPdfDocumentUseCase(FileDocumentVo fileDocumentVo, ChunkPatternVo chunkPatternVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileDocumentVo);

        try {
            PdfDocument pdfDocument = extractPort.extractPdfDocumentPort(fileDocument);
            pdfDocument.extract();
            return this.chunkDocumentUseCase(pdfDocument, chunkPatternVo).stream()
                    .map(patternPassageDocument -> PassageDocumentVo.builder()
                            .docId(patternPassageDocument.getDocId())
                            .depth(patternPassageDocument.getDepth())
                            .tokenSize(patternPassageDocument.getTokenSize())
                            .fullTitle(patternPassageDocument.getFullTitle())
                            .titles(patternPassageDocument.getTitles())
                            .content(patternPassageDocument.getContent())
                            .build())
                    .toList();
        } finally {
            // 파일 삭제
            filePort.clearFilePort(fileDocument);
        }
    }

    /**
     * 법령 문서 청킹
     * @param lawIds 법령 ID 목록
     */
    @Transactional
    public List<PassageDocumentVo> chunkLawDocumentUseCase(List<Long> lawIds) {

        List<PassageDocumentVo> passages = new ArrayList<>();

        String docType      = "DOC-TYPE-DB";
        String categoryCode = "TRAIN-LAW";

        // TODO: 법령 문서 청킹 로직 시작
        for (Long lawId : lawIds) {
            LawDocument lawDocument = lawPersistencePort.getLawDocumentsPort(lawId);
            String docId = String.valueOf(lawDocument.getLawId());

            OriginalDocument originalDocument = OriginalDocument.builder()
                    .docId(docId)
                    .docType(docType)
                    .categoryCode(categoryCode)
                    .name(lawDocument.getLawName())
                    .build();

            for (LawContent lawContent : lawDocument.getLawContents()) {
                log.info("lawContent: {}", lawContent);
                List<LawLink> lawLinks = lawPersistencePort.getLawLinksPort(lawContent.getLawContentId(), lawContent.getVersion());

                for (LawLink lawLink : lawLinks) {
                    log.info("lawLink: {}", lawLink);
                }
            }
        }

        return passages;
    }
}
