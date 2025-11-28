package com.extractor.adapter.out;

import com.extractor.application.port.ExtractPort;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.model.FileDocument;
import com.extractor.global.enums.ExtractType;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.FileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = { ExtractAdapter.class })
class ExtractAdapterTest {

    private final ExtractPort extractPort;

    @Autowired
    public ExtractAdapterTest(ExtractPort extractPort) {
        this.extractPort = extractPort;
    }

    private static final String UPLOAD_PATH = "./filterTest";

    @Test
    @DisplayName("HWPX 한글 문서의 데이터를 추출하여 반환 한다.")
    void extractHwpxPort() {
        // arrange
        String fileId = "hwpx_test_document";
        String originalFileName = "한글_테스트_문서";
        Path path = Paths.get(UPLOAD_PATH);

        FileExtension extension = FileExtension.HWPX;
        String fileName = fileId + "." + extension.getExt();
        Path fullPath = path.resolve(fileName);

        // 압축 해제 경로
        Path unZipPath = path.resolve(fileId);
        path = FileUtil.decompression(fullPath.toFile(), unZipPath.toFile());

        FileDocument fileDocument = FileDocument.builder()
                .originalFileName(originalFileName)
                .path(path)
                .fullPath(fullPath)
                .extension(extension)
                .build();

        // act
        Document document = extractPort.extractHwpxPort(fileDocument, ExtractType.MARK_DOWN);
        HwpxDocument extractHwpxDocument = (HwpxDocument) document;

        // 압축 해제 디렉토리 삭제
        FileUtil.deleteDirectory(fileDocument.getPath());

        // assert
        assertNotNull(extractHwpxDocument);
        assertFalse(extractHwpxDocument.getSections().isEmpty());
    }

    @Test
    @DisplayName("PDF의 데이터를 추출하여 반환 한다.")
    void extractPdfPort() {
        // arrange
        String fileId = "pdf_test_document";
        String originalFileName = "PDF_테스트_문서";
        Path path = Paths.get(UPLOAD_PATH);

        FileExtension extension = FileExtension.PDF;
        String fileName = fileId + "." + extension.getExt();
        Path fullPath = path.resolve(fileName);

        FileDocument fileDocument = FileDocument.builder()
                .originalFileName(originalFileName)
                .path(path)
                .fullPath(fullPath)
                .extension(extension)
                .build();

        // act
        Document document = extractPort.extractPdfPort(fileDocument);
        PdfDocument extractPdfDocument = (PdfDocument) document;

        assertNotNull(extractPdfDocument);
        assertFalse(extractPdfDocument.getContent().isBlank());
    }
}