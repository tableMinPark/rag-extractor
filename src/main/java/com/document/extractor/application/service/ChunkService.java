package com.document.extractor.application.service;

import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.application.port.DocumentReadPort;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.vo.ChunkOptionVo;
import com.document.extractor.application.vo.FileVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.vo.SourceVo;
import com.document.extractor.domain.factory.PassageFactory;
import com.document.extractor.domain.model.Chunk;
import com.document.extractor.domain.model.Document;
import com.document.extractor.domain.model.FileDetail;
import com.document.extractor.domain.model.Passage;
import com.document.extractor.domain.vo.PassageOptionVo;
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
                .originFileName(fileVo.getOriginFileName())
                .fileName(fileVo.getFileName())
                .url(fileVo.getUrl())
                .filePath(fileVo.getFilePath())
                .fileSize(fileVo.getFileSize())
                .ext(fileVo.getExt())
                .url(fileVo.getUrl())
                .build();

        Document document = extractPort.extractFilePort(fileDetail, command.getChunkOption().getExtractType());

        PassageOptionVo passageOptionVo = PassageOptionVo.builder()
                .patterns(chunkOptionVo.getPatterns())
                .type(chunkOptionVo.getSelectType())
                .isExtractTitle(true)
                .build();

        List<Passage> passages = SelectType.TOKEN.equals(chunkOptionVo.getSelectType())
                ? PassageFactory.passaging(document.getDocumentContents(), passageOptionVo, chunkOptionVo.getMaxTokenSize())
                : PassageFactory.passaging(document.getDocumentContents(), passageOptionVo);

        passages.forEach(passage -> passage.setTitle(fileDetail.getOriginFileName()));

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
     * 원격 문서 청킹
     *
     * @param command 원격 문서 청킹 Command
     */
    @Override
    public SourceVo chunkRepoUseCase(ChunkRepoCommand command) {

        ChunkOptionVo chunkOptionVo = command.getChunkOption();

        Document document = documentReadPort.getRepoDocumentPort(command.getRepoType(), command.getRepoId(), chunkOptionVo.getExtractType());
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