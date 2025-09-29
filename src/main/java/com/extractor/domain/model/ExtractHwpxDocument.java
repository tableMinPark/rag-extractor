package com.extractor.domain.model;

import com.extractor.domain.vo.HwpxImageVo;
import com.extractor.domain.vo.HwpxSectionVo;
import com.extractor.global.enums.ExtractType;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.StringUtil;
import com.extractor.global.utils.XmlUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ToString
@Getter
public class ExtractHwpxDocument extends ExtractDocument {

    private final List<HwpxSectionVo> sections;

    private final Map<String, HwpxImageVo> images;

    @Builder
    public ExtractHwpxDocument(String name, FileExtension extension, List<HwpxSectionVo> sections, Map<String, HwpxImageVo> images, Path path) {
        super(name, extension, path);
        this.sections = sections;
        this.images = images;
    }

    /**
     * 추출
     */
    public void extract(ExtractType extractType) {
        this.sections.forEach(section -> {
            Document document = XmlUtil.parseXml(section.getContent());
            Element root = document.getDocumentElement();

            XmlUtil.findChildElementsByTagName(root, "hp:p").forEach(p -> {
                StringBuilder contentBuilder = new StringBuilder();

                for (Element run : XmlUtil.findChildElementsByTagName(p, "hp:run")) {
                    for (Element node : XmlUtil.findChildElements(run)) {
                        switch (node.getNodeName()) {
                            // 텍스트
                            case "hp:t" -> contentBuilder.append(node.getTextContent());
                            // 표
                            case "hp:tbl" -> {
                                Arrays.stream(contentBuilder.toString().split("\n")).forEach(super::addTextContent);
                                String tableContent = this.convertTableXmlToHtml(node, 0);
                                if (ExtractType.MARK_DOWN.equals(extractType)) {
                                    tableContent = StringUtil.convertHtmlToMarkdown(tableContent);
                                }
                                super.addTableContent(tableContent);
                                contentBuilder = new StringBuilder();
                            }
                            // 이미지
                            case "hp:pic" -> {
                                Arrays.stream(contentBuilder.toString().split("\n")).forEach(super::addTextContent);

                                Element img = XmlUtil.findChildElementByTagName(node, "hc:img");

                                if (img != null) {
                                    String id = img.getAttribute("binaryItemIDRef");
                                    super.addImageContent(this.images.containsKey(id) ? this.images.get(id).getContent() : "");
                                }

                                contentBuilder = new StringBuilder();
                            }
                        }
                    }
                }

                if (!contentBuilder.isEmpty()) {
                    Arrays.stream(contentBuilder.toString().split("\n")).forEach(super::addTextContent);
                }
            });
        });
    }

    /**
     * XML 표 데이터 HTML 변환 (재귀)
     *
     * @param element 표 태그 노드
     * @return 표 데이터 HTML 문자열
     */
    private String convertTableXmlToHtml(Element element, int depth) {

        StringBuilder tableHtmlBuilder = new StringBuilder();

        tableHtmlBuilder.append("<table>");

        XmlUtil.findChildElementsByTagName(element, "hp:tr").forEach(tr -> {
            tableHtmlBuilder.append("<tr>");

            XmlUtil.findChildElementsByTagName(tr, "hp:tc").forEach(td -> {
                Element cellSpan = XmlUtil.findChildElementByTagName(td, "hp:cellSpan");

                // 표 병합 체크
                if (cellSpan != null) {
                    String colSpan = cellSpan.getAttribute("colSpan");
                    String rowSpan = cellSpan.getAttribute("rowSpan");

                    if (!"1".equals(colSpan) && !"1".equals(rowSpan)) {
                        tableHtmlBuilder
                                .append("<td")
                                .append(" ").append("colspan=\"").append(colSpan).append("\"")
                                .append(" ").append("rowspan=\"").append(rowSpan).append("\"")
                                .append(">");
                    } else if ("1".equals(colSpan) && !"1".equals(rowSpan)) {
                        tableHtmlBuilder
                                .append("<td")
                                .append(" ").append("rowspan=\"").append(rowSpan).append("\"")
                                .append(">");
                    } else if (!"1".equals(colSpan)) {
                        tableHtmlBuilder
                                .append("<td")
                                .append(" ").append("colspan=\"").append(colSpan).append("\"")
                                .append(">");
                    } else tableHtmlBuilder.append("<td>");
                } else tableHtmlBuilder.append("<td>");

                // 표 셀 데이터 추출
                XmlUtil.findChildElementsByTagName(td, "hp:subList").forEach(subList -> {
                    List<Element> ps = XmlUtil.findChildElementsByTagName(subList, "hp:p");
                    for (int pIndex = 0; pIndex < ps.size(); pIndex++) {
                        XmlUtil.findChildElementsByTagName(ps.get(pIndex), "hp:run").forEach(run ->
                                XmlUtil.findChildElements(run).forEach(node -> {
                                    switch (node.getNodeName()) {
                                        // 텍스트
                                        case "hp:t" -> tableHtmlBuilder.append(node.getTextContent());
                                        // 표
                                        case "hp:tbl" ->
                                                tableHtmlBuilder.append(convertTableXmlToHtml(node, depth + 1));
                                        // 이미지
                                        case "hp:pic" -> {
                                            // TODO: Image -> Text 추출 (OCR)
                                        }
                                    }
                                }));

                        if (pIndex < ps.size() - 1) {
                            tableHtmlBuilder.append("<br>");
                        }
                    }
                });

                tableHtmlBuilder.append("</td>");
            });

            tableHtmlBuilder.append("</tr>");
        });

        tableHtmlBuilder.append("</table>");

        return tableHtmlBuilder.toString();
    }
}