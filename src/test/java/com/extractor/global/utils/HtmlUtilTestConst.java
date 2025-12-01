package com.extractor.global.utils;

public class HtmlUtilTestConst {

    public static final String tableHtml = """
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

    public static final String innerTableHtml = """
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
}
