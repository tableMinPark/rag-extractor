package com.extractor.global.utils;

import com.document.global.utils.HtmlUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class HtmlUtilTest {

    private static final Logger log = LoggerFactory.getLogger(HtmlUtilTest.class);

    private static final String tableHtml = """
    제목
    부제목
    <table>
      <thead>
        <tr>
          <th>A</th>
          <th>B</th>
          <th>C</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>D</td>
          <td>E</td>
          <td>F</td>
        </tr>
        <tr>
          <td>1</td>
          <td>2</td>
          <td>3</td>
        </tr>
        <tr>
          <td>4</td>
          <td>5</td>
          <td>6</td>
        </tr>
      </tbody>
    </table>
    """;

    private static final String innerTableHtml = """
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
    테스트 본문
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

    @DisplayName("중첩 HTML 형식의 표를 특정 DEPTH 까지 삭제 한다.")
    @Test
    void removeHtmlExceptTable() {
        String html = HtmlUtil.removeHtmlExceptTable(innerTableHtml);
        log.info("\n{}", html);
    }

    @DisplayName("MARKDOWN 표를 HTML 형식의 표로 변환 한다.")
    @Test
    void convertMarkDownToHtml() {
        String markdown = HtmlUtil.convertTableHtmlToMarkDown(tableHtml);
        log.info("\n{}", markdown);

        String html = HtmlUtil.convertMarkdownTableToHtml(markdown);
        log.info("\n{}", html);

        Assertions.assertEquals(tableHtml.trim(), html.trim());
    }

    @DisplayName("HTML 표를 MARKDOWN 형식의 표로 변환 한다.")
    @Test
    void convertHtmlToMarkdown() {
        String markdown = HtmlUtil.convertTableHtmlToMarkDown(innerTableHtml);
        log.info("\n{}", markdown);
    }

    @DisplayName("MARKDOWN 표를 HTML 형식의 표로 변환 한다.")
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