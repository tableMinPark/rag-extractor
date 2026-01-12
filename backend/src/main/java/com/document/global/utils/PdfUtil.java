package com.document.global.utils;

import com.document.extractor.domain.vo.PdfSectionVo;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfUtil {

    /**
     * PDF 전체를 페이지 단위로 텍스트만 추출
     */
    public static List<PdfSectionVo> extractByPage(String target) {

        File pdfFile = new File(target);
        if (!pdfFile.exists()) {
            throw new RuntimeException("not found target file");
        }

        List<PdfSectionVo> results = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            SpreadsheetExtractionAlgorithm tableExtractor = new SpreadsheetExtractionAlgorithm();
            PageIterator pi = new ObjectExtractor(document).extract();

            while (pi.hasNext()) {
                Page page = pi.next();
                int pageNumber = page.getPageNumber();

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                String text = stripper.getText(document);
                text = normalize(text);

                List<Table> tables = tableExtractor.extract(page);
                List<List<List<String>>> tableRows = new ArrayList<>();
//                for (Table table : tables) {
//                    List<List<String>> rows = new ArrayList<>();
//                    table.getRows().forEach(row -> {
//                        rows.add(row.stream().map(cell ->
//                            cell.getText().replaceAll("\\s+", " ").trim()).toList());
//                    });
//                    tableRows.add(rows);
//                }

                results.add(new PdfSectionVo(pageNumber, text, tableRows));
            }

        } catch (Exception e) {
            throw new RuntimeException("pdf parsing error", e);
        }

        return results;
    }

    /**
     * 텍스트 정규화 (누락 방지용 최소 처리)
     */
    private static String normalize(String text) {
        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
