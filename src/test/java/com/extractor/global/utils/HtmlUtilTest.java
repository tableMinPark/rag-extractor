package com.extractor.global.utils;

import com.document.global.utils.HtmlUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class HtmlUtilTest {

    private static final String html = """
            제목
            부제목
            <table border="1">
              <tr>
                <th rowspan="2">A</th>
                <th>B</th>
                <th>C</th>
              </tr>
              <tr>
                <th colspan="2">D</th>
              </tr>
              <tr>
                <td>1</td>
                <td rowspan="2" colspan="2">2</td>
              </tr>
              <tr>
                <td>
                    <table border="1">
                      <tr>
                        <th rowspan="2">A</th>
                        <th>B</th>
                        <th>C</th>
                      </tr>
                      <tr>
                        <th colspan="2">D</th>
                      </tr>
                      <tr>
                        <td>1</td>
                        <td rowspan="2" colspan="2">2</td>
                      </tr>
                      <tr>
                        <td>
                            <table border="1">
                              <tr>
                                <th rowspan="2">A</th>
                                <th>B</th>
                                <th>C</th>
                              </tr>
                              <tr>
                                <th colspan="2">D</th>
                              </tr>
                              <tr>
                                <td>1</td>
                                <td rowspan="2" colspan="2">2</td>
                              </tr>
                              <tr>
                                <td>3</td>
                              </tr>
                            </table>
                        </td>
                      </tr>
                    </table>
                </td>
              </tr>
            </table>
    """;
    private static final Logger log = LoggerFactory.getLogger(HtmlUtilTest.class);

    @DisplayName("HTML 표를 MARKDOWN 형식의 표로 변환 한다.")
    @Test
    void convertHtmlToMarkdown() {

        String markdown = HtmlUtil.convertTableHtmlToMarkDown(html);

        log.info("\n{}", markdown);
    }

    @Test
    void convertHtmlToMarkdownTest() {

        String html = """
            <table border="1">
              <tr>
                <th rowspan="2">A</th>
                <th>B</th>
                <th>C</th>
              </tr>
              <tr>
                <th colspan="2">D</th>
              </tr>
              <tr>
                <td>1</td>
                <td rowspan="2" colspan="2">2</td>
              </tr>
              <tr>
                <td>3</td>
              </tr>
            </table>
            """;

        Document doc = Jsoup.parse(html);
        Element table = doc.selectFirst("table");
        if (table == null) return;

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
            sb.append(" |\n");

            // 구분선
            sb.append("| ");
            for (int i = 0; i < maxCols; i++) {
                sb.append("---");
                if (i < maxCols - 1) sb.append(" | ");
            }
            sb.append(" |\n");

            // 나머지 행
            for (int i = 1; i < grid.size(); i++) {
                sb.append("| ");
                sb.append(String.join(" | ", grid.get(i)));
                sb.append(" |\n");
            }
        }

        System.out.println(sb.toString());;
    }
}