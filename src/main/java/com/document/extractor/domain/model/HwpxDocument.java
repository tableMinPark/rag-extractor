package com.document.extractor.domain.model;

import com.document.global.enums.ExtractType;
import com.document.extractor.domain.vo.HwpxImageVo;
import com.document.extractor.domain.vo.HwpxSectionVo;
import com.document.global.utils.HtmlUtil;
import com.document.global.utils.XmlUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ToString
@Getter
public class HwpxDocument extends Document {

    private final List<HwpxSectionVo> sections;

    private final Map<String, HwpxImageVo> images;

    @Builder
    public HwpxDocument(String name, ExtractType extractType, List<HwpxSectionVo> sections, Map<String, HwpxImageVo> images) {
        super(name);
        this.sections = sections;
        this.images = images;
        this.extract(extractType);
    }

    /**
     * 추출
     */
    public void extract(ExtractType extractType) {
        this.clearDocumentContents();
        this.sections.forEach(section -> {
            org.w3c.dom.Document document = XmlUtil.parseXml(section.getContent());
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

                                tableContent = HtmlUtil.removeHtmlExceptTable(tableContent);

                                // 마크 다운 타입인 경우 표 변환
                                if (ExtractType.MARK_DOWN.equals(extractType)) {
                                    tableContent = HtmlUtil.convertTableHtmlToMarkDown(tableContent);
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

        AtomicInteger rowMax = new AtomicInteger();
        AtomicInteger colMax = new AtomicInteger();
        StringBuilder tableHtmlBodyBuilder = new StringBuilder();

        tableHtmlBodyBuilder.append("<tbody>");
        XmlUtil.findChildElementsByTagName(element, "hp:tr").forEach(tr -> {
            rowMax.incrementAndGet();
            tableHtmlBodyBuilder.append("<tr>");

            AtomicInteger colCount = new AtomicInteger();
            XmlUtil.findChildElementsByTagName(tr, "hp:tc").forEach(td -> {
                Element cellSpan = XmlUtil.findChildElementByTagName(td, "hp:cellSpan");

                // 표 병합 체크
                if (cellSpan != null) {
                    String colSpan = cellSpan.getAttribute("colSpan");
                    String rowSpan = cellSpan.getAttribute("rowSpan");
                    colCount.set(colCount.get() + Integer.parseInt(colSpan));

                    if (!"1".equals(colSpan) && !"1".equals(rowSpan)) {
                        tableHtmlBodyBuilder
                                .append("<td")
                                .append(" ").append("colspan=\"").append(colSpan).append("\"")
                                .append(" ").append("rowspan=\"").append(rowSpan).append("\"")
                                .append(">");
                    } else if ("1".equals(colSpan) && !"1".equals(rowSpan)) {
                        tableHtmlBodyBuilder
                                .append("<td")
                                .append(" ").append("rowspan=\"").append(rowSpan).append("\"")
                                .append(">");
                    } else if (!"1".equals(colSpan)) {
                        tableHtmlBodyBuilder
                                .append("<td")
                                .append(" ").append("colspan=\"").append(colSpan).append("\"")
                                .append(">");
                    } else tableHtmlBodyBuilder.append("<td>");
                } else tableHtmlBodyBuilder.append("<td>");

                // 표 셀 데이터 추출
                XmlUtil.findChildElementsByTagName(td, "hp:subList").forEach(subList -> {
                    List<Element> ps = XmlUtil.findChildElementsByTagName(subList, "hp:p");
                    for (int pIndex = 0; pIndex < ps.size(); pIndex++) {
                        XmlUtil.findChildElementsByTagName(ps.get(pIndex), "hp:run").forEach(run ->
                                XmlUtil.findChildElements(run).forEach(node -> {
                                    switch (node.getNodeName()) {
                                        // 텍스트
                                        case "hp:t" -> tableHtmlBodyBuilder.append(node.getTextContent());
                                        // 표
                                        case "hp:tbl" ->
                                                tableHtmlBodyBuilder.append(convertTableXmlToHtml(node, depth + 1));
                                        // 이미지
                                        case "hp:pic" -> {
                                            Element img = XmlUtil.findChildElementByTagName(node, "hc:img");
                                            if (img != null) {
                                                String id = img.getAttribute("binaryItemIDRef");
                                                tableHtmlBodyBuilder.append(this.images.containsKey(id) ? this.images.get(id).getContent() : "");
                                            }
                                        }
                                    }
                                }));

                        if (pIndex < ps.size() - 1) {
                            tableHtmlBodyBuilder.append("<br>");
                        }
                    }
                });

                tableHtmlBodyBuilder.append("</td>");
            });

            colMax.set(Math.max(colMax.get(), colCount.get()));
            tableHtmlBodyBuilder.append("</tr>");
        });

        tableHtmlBodyBuilder.append("</tbody>");


        StringBuilder tableHtmlBuilder = new StringBuilder();

        tableHtmlBuilder.append("<table>");

        if (rowMax.get() <= 1) {
            tableHtmlBuilder.append("<thead>");
            tableHtmlBuilder.append("<tr>");
            tableHtmlBuilder.append("<td></td>".repeat(Math.max(0, colMax.get())));
            tableHtmlBuilder.append("</tr>");
            tableHtmlBuilder.append("</thead>");
        }

        tableHtmlBuilder.append(tableHtmlBodyBuilder);
        tableHtmlBuilder.append("</table>");

        return tableHtmlBuilder.toString();
    }
}