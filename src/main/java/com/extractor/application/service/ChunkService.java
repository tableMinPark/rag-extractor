package com.extractor.application.service;

import com.extractor.application.command.ChunkFileCommand;
import com.extractor.application.command.ChunkLawCommand;
import com.extractor.application.command.ChunkManualCommand;
import com.extractor.application.enums.SelectType;
import com.extractor.application.enums.SourceType;
import com.extractor.application.port.DocumentReadPort;
import com.extractor.application.port.ExtractPort;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.ChunkOptionVo;
import com.extractor.application.vo.FileVo;
import com.extractor.application.vo.PassageVo;
import com.extractor.application.vo.SourceVo;
import com.extractor.domain.factory.PassageFactory;
import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDetail;
import com.extractor.domain.model.Passage;
import com.extractor.domain.vo.PassageOptionVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkService implements ChunkUseCase {

    private final ExtractPort extractPort;
    private final DocumentReadPort documentReadPort;

    /**
     * 파일 청킹
     *
     * @param command 파일 청킹 Command
     */
    @Override
    public SourceVo chunkFileUseCase(ChunkFileCommand command) {

        FileVo fileVo = command.getFile();
        ChunkOptionVo chunkOptionVo = command.getChunkOption();

        FileDetail fileDetail = FileDetail.builder()
                .originalFileName(fileVo.getOriginFileName())
                .fileName(fileVo.getFileName())
                .url(fileVo.getUrl())
                .filePath(fileVo.getFilePath())
                .fileSize(fileVo.getFileSize())
                .ext(fileVo.getExt())
                .url(fileVo.getUrl())
                .build();

        Document document = extractPort.extractFilePort(fileDetail);

        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(chunkOptionVo.getPatterns())
                .type(chunkOptionVo.getSelectType())
                .isExtractTitle(true)
                .build();

        List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, chunkOptionVo.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        passages.forEach(passage -> passage.setTitle(fileDetail.getOriginalFileName()));

        return SourceVo.builder()
                .sourceType(SourceType.FILE.getCode())
                .name(document.getName())
                .content(document.getContent())
                .passageVos(passages.stream()
                        .map(passage -> {
                            List<Chunk> chunks = passage.chunking(chunkOptionVo.getMaxTokenSize(), chunkOptionVo.getOverlapSize());
                            return PassageVo.of(passage, chunks);
                        })
                        .toList())
                .build();
    }

    /**
     * 법령 문서 청킹
     *
     * @param command 법령 문서 청킹 Command
     */
    @Override
    public SourceVo chunkLawUseCase(ChunkLawCommand command) {

        ChunkOptionVo chunkOptionVo = command.getChunkOption();

        Document document = documentReadPort.getLawsPort(command.getLawId(), chunkOptionVo.getExtractType());
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(chunkOptionVo.getPatterns())
                .type(chunkOptionVo.getSelectType())
                .isExtractTitle(false)
                .build();

        List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, chunkOptionVo.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        passages.forEach(passage -> passage.setTitle(document.getName()));

        return SourceVo.builder()
                .sourceType(SourceType.REPO.getCode())
                .name(document.getName())
                .content(document.getContent())
                .passageVos(passages.stream()
                        .map(passage -> {
                            List<Chunk> chunks = passage.chunking(chunkOptionVo.getMaxTokenSize(), chunkOptionVo.getOverlapSize());
                            return PassageVo.of(passage, chunks);
                        })
                        .toList())
                .build();
    }

    /**
     * 메뉴얼 문서 청킹
     *
     * @param command 메뉴얼 문서 청킹 Command
     */
    @Override
    public SourceVo chunkManualUseCase(ChunkManualCommand command) {

        ChunkOptionVo chunkOptionVo = command.getChunkOption();

        Document document = documentReadPort.getManualsPort(command.getManualId(), chunkOptionVo.getExtractType());
        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(chunkOptionVo.getPatterns())
                .type(chunkOptionVo.getSelectType())
                .isExtractTitle(false)
                .build();

        List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, chunkOptionVo.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        passages.forEach(passage -> passage.setTitle(document.getName()));

        return SourceVo.builder()
                .sourceType(SourceType.REPO.getCode())
                .name(document.getName())
                .content(document.getContent())
                .passageVos(passages.stream()
                        .map(passage -> {
                            List<Chunk> chunks = passage.chunking(chunkOptionVo.getMaxTokenSize(), chunkOptionVo.getOverlapSize());
                            return PassageVo.of(passage, chunks);
                        })
                        .toList())
                .build();
    }
}