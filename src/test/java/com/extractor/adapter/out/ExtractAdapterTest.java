package com.extractor.adapter.out;

import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.port.ExtractPort;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.global.enums.FileExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = { ExtractAdapter.class })
class ExtractAdapterTest {

    private final ExtractPort extractPort;

    @Autowired
    public ExtractAdapterTest(ExtractPort extractPort) {
        this.extractPort = extractPort;
    }

    @Test
    @DisplayName("HWPX 한글 문서를 압축 해제 후, 데이터를 추출하여 반환 한다.")
    void extractHwpxDocumentPort() {
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
        HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(originalDocumentVo);

        var expected = FileUtil.deleteDirectory(hwpxDocument.getUnZipPath());

        // assert
        assertNotNull(hwpxDocument);
        assertTrue(expected);
    }

    @Test
    @DisplayName("PDF 한글 문서를 SNF을 통해 추출하여 반환 한다.")
    void extractPdfDocumentPort() {
        // arrange
        String fileName = "test_document.pdf";
        String originalFileName = "test_document.pdf";
        String extension = FileExtension.PDF.getExtension();
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
        PdfDocument pdfDocument = extractPort.extractPdfDocumentPort(originalDocumentVo);

        assertNotNull(pdfDocument);
        assertFalse(pdfDocument.getContent().isBlank());
    }
}