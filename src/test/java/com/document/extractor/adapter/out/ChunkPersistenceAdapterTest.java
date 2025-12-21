package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.ChunkEntity;
import com.document.extractor.adapter.out.entity.ChunkOriginEntityForTest;
import com.document.extractor.adapter.out.entity.ChunkTmpEntityForTest;
import com.document.extractor.adapter.out.repository.ChunkOriginRepository;
import com.document.extractor.adapter.out.repository.ChunkRepository;
import com.document.extractor.adapter.out.repository.ChunkTmpRepository;
import com.document.extractor.domain.model.Chunk;
import com.document.global.utils.HtmlUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
class ChunkPersistenceAdapterTest {

    private static final Logger log = LoggerFactory.getLogger(ChunkPersistenceAdapterTest.class);
    @Autowired
    private ChunkOriginRepository chunkOriginRepository;
    @Autowired
    private ChunkTmpRepository chunkTmpRepository;
    @Autowired
    private ChunkRepository chunkRepository;

    /**
     * 실행 전 passageId 업데이트 쿼리 실행 필요
     * UPDATE WN_CHUNK_TMP WCT
     *   SET WCT.PASSAGE_ID = (
     *   	SELECT WP.PASSAGE_ID
     *   FROM WN_CHUNK_ORIGIN WCO
     *   JOIN WN_PASSAGE WP ON WCO.PASSAGE_ID = WP.PASSAGE_ID
     *  WHERE WCO.CHUNK_ID = WCT.CHUNK_ID
     * )
     * ;
     */
    @Commit
    @Transactional
    @DisplayName("패시지 ID 가 없는 청크의 패시지 ID 를 찾고 새로운 청크를 등록 한다.")
    @Test
    void chunkBatchTest() {

        int page = 0;
        int size = 100;
        int totalCount = 0;
        int createCount = 0;
        int updateCount = 0;

        Page<ChunkOriginEntityForTest> originTemps;

        do {
            page++;
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "chunkId"));

            originTemps = chunkOriginRepository.findAll(pageable);

            for (ChunkOriginEntityForTest originTemp : originTemps) {
                List<ChunkTmpEntityForTest> temps = chunkTmpRepository.findAllByChunkIdOrderById(originTemp.getChunkId());

                totalCount += temps.size();
                for (ChunkTmpEntityForTest temp : temps) {
                    ChunkEntity chunkEntity = ChunkEntity.builder()
                            .passageId(originTemp.getPassageId())
                            .version(originTemp.getVersion())
                            .subContent(originTemp.getSubContent())
                            .compactContent("")
                            .compactTokenSize(0)
                            .title(temp.getTitle())
                            .subTitle(temp.getSubTitle())
                            .thirdTitle(temp.getThirdTitle())
                            .content(temp.getContent())
                            .tokenSize(temp.getTokenSize())
                            .build();
                    chunkRepository.save(chunkEntity);
                    createCount++;
                }
            }

            log.info("W) page: {}/{}, size: {}, totalCount: {}", page, originTemps.getTotalPages(), size, totalCount);

        } while (page < originTemps.getTotalPages());

        log.info("update: {}, create: {}, total: {}", updateCount, createCount, updateCount + createCount);
    }

    @Commit
    @Transactional
    @DisplayName("content 를 기반으로 compactContent 를 수정 한다.")
    @Test
    void convertCompactContentTest() {

        int page = 0;
        int size = 100;
        int totalCount = 0;
        int updateCount = 0;

        Page<ChunkEntity> temps;
        do {
            page++;
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "chunkId"));
            temps = chunkRepository.findAll(pageable);


            totalCount += temps.getContent().size();
            for (ChunkEntity temp : temps) {
                Chunk chunk = temp.toDomain();

                String content = chunk.getContent() == null
                        ? ""
                        : chunk.getContent();

                String compactContent = chunk.getContent() == null
                        ? ""
                        : HtmlUtil.convertTableHtmlToMarkdown(chunk.getContent());

                chunk.update(
                        chunk.getTitle(),
                        chunk.getSubTitle(),
                        chunk.getThirdTitle(),
                        content,
                        chunk.getSubContent(),
                        compactContent,
                        content.length(),
                        compactContent.length());

                temp.update(chunk);
                chunkRepository.save(temp);
            }

            log.info("W) page: {}/{}, size: {}, totalCount: {}", page, temps.getTotalPages(), size, totalCount);

        } while (page < temps.getTotalPages());

        log.info("update: {}", updateCount);
    }
}