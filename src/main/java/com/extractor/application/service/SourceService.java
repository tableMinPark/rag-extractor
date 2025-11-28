package com.extractor.application.service;

import com.extractor.application.port.SourcePersistencePort;
import com.extractor.application.usecase.SourceUseCase;
import com.extractor.application.vo.ChunkVo;
import com.extractor.application.vo.PassageVo;
import com.extractor.application.vo.SourceOptionVo;
import com.extractor.application.vo.SourceVo;
import com.extractor.domain.model.Chunk;
import com.extractor.domain.model.Passage;
import com.extractor.domain.model.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SourceService implements SourceUseCase {

    private final SourcePersistencePort sourcePersistencePort;

    /**
     * 전처리 결과 등록
     *
     * @param sourceOptionVo 결과 등록 옵션
     * @param sourceVos      전처리 결과 목록
     */
    @Transactional
    @Override
    public void createSource(SourceOptionVo sourceOptionVo, List<SourceVo> sourceVos) {

        long fileDetailId = 1;

        for (SourceVo sourceVo : sourceVos) {
            Source source = sourcePersistencePort.createSourcePort(Source.builder()
                    .sourceId(sourceVo.getSourceId())
                    .version(sourceVo.getVersion())
                    .sourceType(sourceVo.getSourceType())
                    .categoryCode(sourceVo.getCategoryCode())
                    .name(sourceVo.getName())
                    .content(sourceVo.getContent())
                    .collectionId(sourceOptionVo.getCollectionId())
                    .fileDetailId(fileDetailId)
                    .build());

            for (PassageVo passageVo : sourceVo.getPassageVos()) {
                Passage passage = sourcePersistencePort.createPassagePort(Passage.builder()
                        .sourceId(source.getSourceId())
                        .title(passageVo.getTitle())
                        .subTitle(passageVo.getSubTitle())
                        .thirdTitle(passageVo.getThirdTitle())
                        .content(passageVo.getContent())
                        .subContent(passageVo.getSubContent())
                        .tokenSize(passageVo.getTokenSize())
                        .build());

                for (ChunkVo chunkVo : passageVo.getChunkVos()) {
                    sourcePersistencePort.createChunkPort(Chunk.builder()
                            .passageId(passage.getPassageId())
                            .title(chunkVo.getTitle())
                            .subTitle(chunkVo.getSubTitle())
                            .thirdTitle(chunkVo.getThirdTitle())
                            .content(chunkVo.getContent())
                            .subContent(chunkVo.getSubContent())
                            .tokenSize(chunkVo.getTokenSize())
                            .build());
                }
            }
        }
    }
}
