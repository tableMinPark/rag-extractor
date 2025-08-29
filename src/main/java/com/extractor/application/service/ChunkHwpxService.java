package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.usecase.ChunkHwpxUseCase;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.global.utils.XmlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkHwpxService implements ChunkHwpxUseCase {

    private final ExtractPort extractPort;

    /**
     * 한글 문서 청킹
     *
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public HwpxDocument chunkHwpxDocument(OriginalDocumentVo originalDocumentVo, ChunkPatternVo chunkPatternVo) {

        HwpxDocument hwpxDocument = extractPort.extractHwpxDocumentPort(originalDocumentVo);

        // section 파싱
        hwpxDocument.getSections().forEach(section -> {
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
                                Arrays.stream(contentBuilder.toString().split("\n")).forEach(hwpxDocument::addText);
                                hwpxDocument.addTable(convertTableXmlToHtml(node, 0));
                                contentBuilder = new StringBuilder();
                            }
                            // 이미지
                            case "hp:pic" -> {
                                Arrays.stream(contentBuilder.toString().split("\n")).forEach(hwpxDocument::addText);
                                // TODO: Image -> Text 추출 (OCR)
                                hwpxDocument.addImage("<IMAGE/>");
                                contentBuilder = new StringBuilder();
                            }
                        }
                    }
                }

                if (!contentBuilder.isEmpty()) {
                    Arrays.stream(contentBuilder.toString().split("\n")).forEach(hwpxDocument::addText);
                }
            });
        });

        // 패시지 분류
        hwpxDocument.selectPassage(chunkPatternVo);

        return hwpxDocument;
    }

    /**
     * XML 표 데이터 HTML 변환 (재귀)
     * @param element 표 태그 노드
     * @return 표 데이터 HTML 문자열
     */
    private static String convertTableXmlToHtml(Element element, int depth) {

        StringBuilder tableHtmlBuilder  = new StringBuilder();

        tableHtmlBuilder.append("<table>");

        XmlUtil.findChildElementsByTagName(element, "hp:tr").forEach(tr -> {
            tableHtmlBuilder.append("<tr>");

            XmlUtil.findChildElementsByTagName(tr, "hp:tc").forEach(td -> {
                Element cellSpan = XmlUtil.findChildElementByTagName(td, "hp:cellSpan");

                // 표 병합 체크
                if (cellSpan != null) {
                    String colSpan = cellSpan.getAttribute("colSpan");
                    String rowSpan = cellSpan.getAttribute("rowSpan");

                    if (!"1".equals(colSpan) &&  !"1".equals(rowSpan)) {
                        tableHtmlBuilder
                                .append("<td")
                                .append(" ").append("colspan=\"").append(colSpan).append("\"")
                                .append(" ").append("rowspan=\"").append(rowSpan).append("\"")
                                .append(">");
                    } else if ("1".equals(colSpan) &&  !"1".equals(rowSpan)) {
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
                        XmlUtil.findChildElementsByTagName(ps.get(pIndex), "hp:run").forEach(run -> {
                            XmlUtil.findChildElements(run).forEach(node -> {
                                switch (node.getNodeName()) {
                                    // 텍스트
                                    case "hp:t" -> tableHtmlBuilder.append(node.getTextContent());
                                    // 표
                                    case "hp:tbl" -> tableHtmlBuilder.append(convertTableXmlToHtml(node, depth + 1));
                                    // 이미지
                                    case "hp:pic" -> {
                                        // TODO: Image -> Text 추출 (OCR)
                                    }
                                }
                            });
                        });

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
