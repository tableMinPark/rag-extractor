package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.port.LawReadPort;
import com.extractor.application.port.ManualReadPort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.ChunkOptionVo;
import com.extractor.application.vo.FileVo;
import com.extractor.application.vo.PassageVo;
import com.extractor.application.vo.SourceVo;
import com.extractor.domain.factory.PassageFactory;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDocument;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.PassageOption;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.enums.SelectType;
import com.extractor.global.enums.SourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ChunkService implements ChunkUseCase {

    private final ExtractPort extractPort;
    private final FilePort filePort;
    private final LawReadPort lawReadPort;
    private final ManualReadPort manualReadPort;

    @Autowired
    public ChunkService(ExtractPort extractPort, FilePort filePort, LawReadPort lawReadPort, ManualReadPort manualReadPort) {
        this.extractPort = extractPort;
        this.filePort = filePort;
        this.lawReadPort = lawReadPort;
        this.manualReadPort = manualReadPort;
    }

    /**
     * 파일 청킹
     *
     * @param chunkOptionVo 청킹 옵션
     * @param fileVo        파일
     */
    @Override
    public SourceVo chunkFileUseCase(ChunkOptionVo chunkOptionVo, FileVo fileVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileVo);

        Document document;
        if (FileExtension.PDF.equals(fileDocument.getExtension())) {
            document = extractPort.extractPdfPort(fileDocument);
        } else {
            document = extractPort.extractHwpxPort(fileDocument, chunkOptionVo.getExtractType());
        }

        try {
            PassageOption passageOption = PassageOption.builder()
                    .patterns(chunkOptionVo.getPatterns())
                    .type(chunkOptionVo.getSelectType())
                    .isExtractTitle(chunkOptionVo.isExtractTitle())
                    .build();

            List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                    ? PassageFactory.passaging(document.getDocumentContents(), passageOption, chunkOptionVo.getMaxTokenSize())
                    : PassageFactory.passaging(document.getDocumentContents(), passageOption);

            passages.forEach(passage -> passage.setTitle(fileDocument.getOriginalFileName()));

            return SourceVo.builder()
                    .sourceType(SourceType.FILE.getCode())
                    .name(document.getName())
                    .content(document.getContent())
                    .passageVos(passages.stream()
                            .map(passage -> PassageVo.of(passage)
                                    .chunking(chunkOptionVo.getMaxTokenSize(), chunkOptionVo.getOverlapSize()))
                            .toList())
                    .build();
        } finally {
            // 파일 삭제
            filePort.removeFilePort(fileDocument);
        }
    }

    /**
     * 법령 문서 청킹
     *
     * @param chunkOptionVo 청킹 패턴 정보
     * @param lawId         법령 ID
     */
    @Override
    @Transactional
    public SourceVo chunkLawUseCase(ChunkOptionVo chunkOptionVo, Long lawId) {

        Document document = lawReadPort.getLawsPort(lawId, chunkOptionVo.getExtractType());
        PassageOption passageOption = PassageOption.builder()
                .patterns(chunkOptionVo.getPatterns())
                .type(chunkOptionVo.getSelectType())
                .isExtractTitle(chunkOptionVo.isExtractTitle())
                .build();

        List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOption, chunkOptionVo.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOption);

        passages.forEach(passage -> passage.setTitle(document.getName()));

        return SourceVo.builder()
                .sourceType(SourceType.DB.getCode())
                .name(document.getName())
                .content(document.getContent())
                .passageVos(passages.stream()
                        .map(passage -> PassageVo.of(passage)
                                    .chunking(chunkOptionVo.getMaxTokenSize(), chunkOptionVo.getOverlapSize()))
                        .toList())
                .build();
    }

    /**
     * 메뉴얼 문서 청킹
     *
     * @param chunkOptionVo 청킹 패턴 정보
     * @param manualId      메뉴얼 ID
     */
    @Override
    public SourceVo chunkManualUseCase(ChunkOptionVo chunkOptionVo, Long manualId) {

        Document document = manualReadPort.getManualsPort(manualId, chunkOptionVo.getExtractType());
        PassageOption passageOption = PassageOption.builder()
                .patterns(chunkOptionVo.getPatterns())
                .type(chunkOptionVo.getSelectType())
                .isExtractTitle(chunkOptionVo.isExtractTitle())
                .build();

        List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOption, chunkOptionVo.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOption);

        passages.forEach(passage -> passage.setTitle(document.getName()));

        return SourceVo.builder()
                .sourceType(SourceType.DB.getCode())
                .name(document.getName())
                .content(document.getContent())
                .passageVos(passages.stream()
                        .map(passage -> PassageVo.of(passage)
                                .chunking(chunkOptionVo.getMaxTokenSize(), chunkOptionVo.getOverlapSize()))
                        .toList())
                .build();
    }
}