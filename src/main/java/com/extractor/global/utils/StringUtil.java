package com.extractor.global.utils;

import com.extractor.global.enums.ExtractType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StringUtil {

    // 원형 숫자 기호 (0 ~ 49)
    private static final char[] CIRCLE_NUMBERS = {
            0x24EA, 0x2460, 0x2461, 0x2462, 0x2463, 0x2464, 0x2465, 0x2466, 0x2467, 0x2468,
            0x2469, 0x246A, 0x246B, 0x246C, 0x246D, 0x246E, 0x246F, 0x2470, 0x2471, 0x2472,
            0x2473, 0x3251, 0x3252, 0x3253, 0x3254, 0x3255, 0x3256, 0x3257, 0x3258, 0x3259,
            0x325A, 0x325B, 0x325C, 0x325D, 0x325E, 0x325F, 0x32B1, 0x32B2, 0x32B3, 0x32B4,
            0x32B5, 0x32B6, 0x32B7, 0x32B8, 0x32B9, 0x32BA, 0x32BB, 0x32BC, 0x32BD, 0x32BE
    };

    private static final Set<String> TABLE_TAGS = Set.of("table", "tr", "td", "thead", "tbody", "th");
    private static final Set<String> BLOCK_TAGS = Set.of(
            "div", "p", "section", "article", "header", "footer", "aside", "nav", "main",
            "li", "tr", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "br", "hr"
    );

    /**
     * 랜덤 ID 값 생성
     *
     * @return 랜덤 ID
     */
    public static String generateRandomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 문자열 숫자 여부 확인
     *
     * @param str 문자열
     * @return 숫자 여부
     */
    public static boolean isNumber(String str) {
        if (str != null && !str.isBlank()) {
            return !str.chars().allMatch(Character::isDigit);
        }
        return true;
    }

    /**
     * HTML 표 데이터 마크 다운 변환
     *
     * @param html HTML 문자열
     * @return 표 Markdown 변환 된, HTML 문자열
     */
    private static String convertTableHtmlToMarkDown(String html) {

        Document doc = Jsoup.parse(html);
        StringBuilder tableMarkdownBuilder = new StringBuilder();

        convertTableHtmlToMarkDown(doc.body(), tableMarkdownBuilder, -1);

        String convertMarkdown = doc.outerHtml().replace("&lt;br&gt;", "\n<br>");

        if (!tableMarkdownBuilder.toString().trim().isBlank()) {
            convertMarkdown += "\n<br>\n<br>---\n<br>\n<br>" + tableMarkdownBuilder.toString().trim();
        }

        return normalize(convertMarkdown);
    }

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
                    node.replaceWith(new TextNode(convertTableHtmlToMarkdownOneDepth(node.toString(), "\n<br>")));
                } else if (depth > 0) {
                    String tableId = generateRandomId();
                    // 표 마크 다운 내부 개행 기호 => "\\n"
                    node.replaceWith(new TextNode("\\n[" + tableId + "](#" + tableId + ")\\n"));
                    tableMarkdownBuilder
                            .append("#").append(" ").append(tableId).append("\n<br>")
                            .append(convertTableHtmlToMarkdownOneDepth(node.toString(), "\n<br>"))
                            .append("\n<br>\n<br>");
                }
            }
        }
    }

    /**
     * 표 데이터 HTML 마크 다운 1Depth 변환
     *
     * @param html HTML 문자열
     * @return 표 Markdown 변환 된, HTML 문자열
     */
    public static String convertTableHtmlToMarkdownOneDepth(String html) {
        return convertTableHtmlToMarkdownOneDepth(html, "\n");
    }

    /**
     * 표 데이터 HTML 마크 다운 1Depth 변환
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
            sb.append("| ");
            sb.append(String.join(" | ", grid.getFirst()));
            sb.append(" |");
            sb.append("\n");

            // 구분선
            sb.append("| ");
            for (int i = 0; i < maxCols; i++) {
                sb.append("---");
                if (i < maxCols - 1) sb.append(" | ");
            }
            sb.append(" |");
            sb.append("\n");

            // 나머지 행
            for (int i = 1; i < grid.size(); i++) {
                sb.append("| ");
                sb.append(String.join(" | ", grid.get(i)));
                sb.append(" |");
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
    public static String removeHtmlExceptTable(String html, ExtractType extractType) {
        // table 태그 표 마크 다운 문자열 변환
        String convertTableHtml = html;

        if (ExtractType.MARK_DOWN.equals(extractType)) {
            convertTableHtml = convertTableHtmlToMarkDown(html);
        }

        Document doc = Jsoup.parse(convertTableHtml);

        StringBuilder stringBuilder = new StringBuilder();

        // HTML 태그 삭제
        removeHtmlExceptTable(doc.body(), stringBuilder);

        // 표 마크 다운 셀 내부 개행 처리
        return normalize(stringBuilder.toString().replace("\\n", "<br>"));
    }

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

    /**
     * 공백/개행 정리
     *
     * @param str 원본 문자열
     * @return 공백 정리 문자열
     */
    private static String normalize(String str) {
        str = str.replaceAll("[ \\t\\f\\r]+", " ");   // 연속 공백 → 하나
        str = str.replaceAll(" *\\n+ *", "\n");       // 개행 여러 개 → 하나
        return str.trim();
    }

    /**
     * 원형 숫자 기호 대치
     *
     * @param c 문자
     * @return 대치 문자열
     */
    public static String getCircleNumber(char c) {
        for (int num = 0; num < CIRCLE_NUMBERS.length; num++) {
            if (c == CIRCLE_NUMBERS[num]) {
                return String.valueOf(num);
            }
        }

        return String.valueOf(c);
    }

    /**
     * 문자열 개행 기준 병합
     */
    public static String concat(List<String> strs) {

        if (strs == null || strs.isEmpty()) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder();

        for (String str : strs) {
            strBuilder.append(str).append("\n");
        }

        return strBuilder.toString().trim();
    }
}