package com.document.global.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HtmlUtil {

    private static final Set<String> TABLE_TAGS = Set.of("table", "tr", "td", "thead", "tbody", "th");
    private static final Set<String> BLOCK_TAGS = Set.of(
            "div", "p", "section", "article", "header", "footer", "aside", "nav", "main",
            "li", "tr", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "br", "hr"
    );

    /**
     * HTML 표 데이터 마크 다운 변환
     *
     * @param html HTML 문자열
     * @return 표 Markdown 변환 된, HTML 문자열
     */
    public static String convertTableHtmlToMarkDown(String html) {
        // 개형 변환
        String convertNewLineHtml = html.replace("\n", "<br>");

        Document doc = Jsoup.parse(convertNewLineHtml);
        StringBuilder tableMarkdownBuilder = new StringBuilder();

        convertTableHtmlToMarkDown(doc.body(), tableMarkdownBuilder, -1);

        String convertMarkdown = doc.body().html()
                .replace("<br>", "\n")
                .replace("&lt;br&gt;", "\n");

        if (!tableMarkdownBuilder.toString().trim().isBlank()) {
            convertMarkdown += "\n---\n" + tableMarkdownBuilder.toString().trim();
        }

        return StringUtil.normalize(convertMarkdown.replace("\\n", "<br>"));
    }

    /**
     * HTML 표 데이터 마크 다운 변환 재귀 함수
     */
    private static void convertTableHtmlToMarkDown(Node node, StringBuilder tableMarkdownBuilder, int depth) {

        // 표안의 표 처리
        for (Node child : node.childNodes()) {
            if (child instanceof Element el) {
                String tag = el.tagName();

                if ("table".equals(tag)) {
                    convertTableHtmlToMarkDown(child, tableMarkdownBuilder, depth + 1);
                } else {
                    convertTableHtmlToMarkDown(child, tableMarkdownBuilder, depth);
                }
            }
        }

        if (node instanceof Element el) {
            String tag = el.tagName();

            if ("table".equals(tag)) {
                if (depth == 0) {
                    node.replaceWith(new TextNode(convertTableHtmlToMarkdownOneDepth(node.toString(), "<br>")));
                } else if (depth > 0) {
                    String tableId = StringUtil.generateRandomId();
                    // 표 마크 다운 내부 개행 기호 => "\\n"
                    node.replaceWith(new TextNode("\\n[" + tableId + "](#" + tableId + ")\\n"));
                    tableMarkdownBuilder
                            .insert(0, "\n\n\n")
                            .insert(0, convertTableHtmlToMarkdownOneDepth(node.toString(), "\n"))
                            .insert(0, String.format("# %s\n", tableId));
                }
            }
        }
    }

    /**
     * 표 데이터 HTML 마크 다운 1Depth 변환 재귀 함수
     *
     * @param html             HTML 문자열
     * @param newLineSeparator 개행 구분자
     * @return 마크 다운 문자열
     */
    private static String convertTableHtmlToMarkdownOneDepth(String html, String newLineSeparator) {

        Document doc = Jsoup.parse(html);
        Element table = doc.selectFirst("table");
        if (table == null) return "";

        List<List<String>> grid = new ArrayList<>();
        Elements rows = table.select("tr");

        for (int r = 0; r < rows.size(); r++) {
            Element tr = rows.get(r);
            // 현재 row 준비
            while (grid.size() <= r) {
                grid.add(new ArrayList<>());
            }

            Elements cells = tr.select("th, td");
            int cIdx = 0;

            for (Element cell : cells) {
                // 이미 채워진 칸 건너뛰기
                while (cIdx < grid.get(r).size() && grid.get(r).get(cIdx) != null) {
                    cIdx++;
                }

                int rowspan = cell.hasAttr("rowspan") ? Integer.parseInt(cell.attr("rowspan")) : 1;
                int colspan = cell.hasAttr("colspan") ? Integer.parseInt(cell.attr("colspan")) : 1;

                String text = cell.text().trim().replaceAll("\\s+", " ");

                // 필요한 만큼 row 확보
                while (grid.size() < r + rowspan) {
                    grid.add(new ArrayList<>());
                }

                // 각 row에 충분한 열 확보
                for (int rr = r; rr < r + rowspan; rr++) {
                    List<String> row = grid.get(rr);
                    while (row.size() < cIdx + colspan) {
                        row.add(null);
                    }
                }

                // 병합된 범위 모두 같은 값으로 채우기
                for (int rr = r; rr < r + rowspan; rr++) {
                    for (int cc = cIdx; cc < cIdx + colspan; cc++) {
                        grid.get(rr).set(cc, text);
                    }
                }

                cIdx += colspan;
            }
        }

        // 최대 열 수 맞추기
        int maxCols = grid.stream().mapToInt(List::size).max().orElse(0);
        for (List<String> row : grid) {
            while (row.size() < maxCols) {
                row.add("");
            }
        }

        // Markdown 문자열 만들기
        StringBuilder sb = new StringBuilder();
        if (!grid.isEmpty()) {
            // 헤더
            sb.append("|");
            sb.append(String.join("|", grid.getFirst()));
            sb.append("|");
            sb.append("\n");

            // 구분선
            sb.append("|");
            for (int i = 0; i < maxCols; i++) {
                sb.append("---");
                if (i < maxCols - 1) sb.append("|");
            }
            sb.append("|");
            sb.append("\n");

            // 나머지 행
            for (int i = 1; i < grid.size(); i++) {
                sb.append("|");
                sb.append(String.join("|", grid.get(i)));
                sb.append("|");
                sb.append("\n");
            }
        }

        return sb.toString().trim().replace("\n", newLineSeparator);
    }

    /**
     * HTML 태그 삭제 (표 HTML 보존)
     *
     * @param html HTML 문자열
     * @return HTML 태그 삭제 문자열
     */
    public static String removeHtmlExceptTable(String html) {
        // 개형 변환
        String convertNewLineHtml = html.replace("\n", "<br>");

        // table 태그 표 마크 다운 문자열 변환
        Document doc = Jsoup.parse(convertNewLineHtml);

        StringBuilder stringBuilder = new StringBuilder();
        // HTML 태그 삭제
        removeHtmlExceptTable(doc.body(), stringBuilder);

        // 표 마크 다운 셀 내부 개행 처리
        return StringUtil.normalize(stringBuilder.toString().replace("<br>", "\n"));
    }

    /**
     * HTML 태그 삭제 (표 HTML 보존) 재귀 함수
     */
    private static void removeHtmlExceptTable(Node node, StringBuilder stringBuilder) {
        for (Node child : node.childNodes()) {
            if (child instanceof TextNode) {
                stringBuilder.append(((TextNode) child).text());
            } else if (child instanceof Element el) {
                String tag = el.tagName().toLowerCase();

                if (TABLE_TAGS.contains(tag)) {
                    // table 구조 태그 유지 + 속성 포함
                    stringBuilder.append("<").append(tag);

                    // 속성 보존 (colspan, rowspan)
                    for (Attribute attr : el.attributes()) {
                        if (Set.of("colspan", "rowspan").contains(attr.getKey())) {
                            stringBuilder.append(" ")
                                    .append(attr.getKey())
                                    .append("=\"").append(attr.getValue()).append("\"");
                        }
                    }

                    stringBuilder.append(">");
                    removeHtmlExceptTable(el, stringBuilder);
                    stringBuilder.append("</").append(tag).append(">");
                } else {
                    removeHtmlExceptTable(el, stringBuilder);
                    if (BLOCK_TAGS.contains(tag)) {
                        stringBuilder.append("\n");
                    }
                }
            }
        }
    }
}
