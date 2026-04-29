package com.genai.extractor.service.domain.model;

import com.genai.extractor.enums.ExtractType;
import com.genai.extractor.service.vo.PdfSectionVo;
import com.genai.common.utils.HtmlUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@ToString
@Getter
public class PdfDocument extends Document {

    private final List<PdfSectionVo> sections;

    @Builder
    public PdfDocument(String name, String extractTypeCode, List<PdfSectionVo> sections, boolean convertError) {
        super(name, convertError);
        this.sections = sections;
        this.extract(ExtractType.find(extractTypeCode));
    }

    /**
     * 추출
     */
    public void extract(ExtractType extractType) {
        this.clearDocumentContents();
        this.sections.forEach(section -> {
            Arrays.stream(section.getText().split("\n")).forEach(super::addTextContent);

            for (List<List<String>> table : section.getTables()) {
                StringBuilder tableHtmlBuilder = new StringBuilder();

                tableHtmlBuilder.append("<table>");
                tableHtmlBuilder.append("<tbody>");
                for (List<String> row : table) {

                    tableHtmlBuilder.append("<tr>");
                    for (String cell : row) {
                        tableHtmlBuilder.append("<td>");
                        tableHtmlBuilder.append(cell);
                        tableHtmlBuilder.append("</td>");
                    }
                    tableHtmlBuilder.append("</tr>");
                }
                tableHtmlBuilder.append("</tbody>");
                tableHtmlBuilder.append("</table>");

                String tableContent = HtmlUtil.removeHtmlExceptTable(tableHtmlBuilder.toString());

                if (ExtractType.MARKDOWN.equals(extractType)) {
                    tableContent = HtmlUtil.convertTableHtmlToMarkdown(tableContent);
                }

                super.addTableContent(tableContent);
            }
        });
    }
}