package com.extractor.application.service;

import com.extractor.adapter.out.ExtractAdapter;
import com.extractor.adapter.utils.FileUtil;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.domain.vo.pattern.PatternVo;
import com.extractor.global.enums.FileExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ChunkService.class, ExtractAdapter.class })
class ChunkServiceTest {

    private final ChunkService chunkService;

    @Autowired
    public ChunkServiceTest(ChunkService chunkService) {
        this.chunkService = chunkService;
    }

    @Test
    void chunkHwpxDocumentTest() {
        // arrange
        String fileName = "test_document.hwpx";
        String originalFileName = "test_document.hwpx";
        String extension = FileExtension.HWPX.getExtension();
        Path path = Paths.get("./filter");
        Path fullPath = Paths.get("./filter", fileName);

        OriginalDocumentVo originalDocumentVo = OriginalDocumentVo.builder()
                .fileName(fileName)
                .originalFileName(originalFileName)
                .path(path)
                .fullPath(fullPath)
                .extension(extension)
                .build();

        // act
        HwpxDocument hwpxDocument = chunkService.chunkHwpxDocument(
            originalDocumentVo,
            new ChunkPatternVo(
                List.of(
                    new PatternVo(0,"^[\\[\\(][^\\]\\)]*별표[^\\]\\)]*[\\]\\)]"),
                    new PatternVo(0,"^부\\s+칙"),
                    new PatternVo(0,"^제[0-9]{1,3}장[ 가-힣]+"),
                    new PatternVo(1,"^제[0-9]{1,3}절[ 가-힣]+"),
                    new PatternVo(2,"^제[0-9]{1,3}조\\([ 가-힣]+\\)")
                ),
                List.of(new PatternVo(0,"^[\\[\\(][^\\]\\)]*(별지|별첨|서식)[^\\]\\)]*[\\]\\)]"))
            )
        );

        hwpxDocument.getPassages().forEach(vo -> {
            System.out.println("[" + vo.getNum() + "] | " + Arrays.toString(vo.getTitles()) + " | " + vo.getContent().replace("\n", " "));
        });

        // 압축 파일 삭제
        var expected = FileUtil.deleteDirectory(hwpxDocument.getUnZipPath());

        // assert
        assertNotNull(hwpxDocument);
        assertTrue(expected);
    }
}