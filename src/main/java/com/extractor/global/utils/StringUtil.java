package com.extractor.global.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StringUtil {

    private static final Set<String> BLOCK_TAGS = new HashSet<>(Arrays.asList(
            "div", "p", "section", "article", "header", "footer", "aside", "nav", "main",
            "li", "tr", "h1", "h2", "h3", "h4", "h5", "h6",
            "blockquote", "br", "hr"
    ));

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
            return str.chars().allMatch(Character::isDigit);
        }
        return false;
    }

    /**
     * HTML 태그 삭제
     *
     * @param str 문자열
     * @return HTML 태그 삭제 문자열
     */
    public static String removeHtml(String str) {
        Document doc = Jsoup.parse(str);
        StringBuilder sb = new StringBuilder();
        appendNodes(doc.body(), sb);
        return normalize(sb.toString());
    }

    private static void appendNodes(Node node, StringBuilder sb) {
        for (Node child : node.childNodes()) {
            if (child instanceof TextNode) {
                sb.append(((TextNode) child).text());
            } else if (child instanceof Element el) {
                appendNodes(el, sb);
                String tag = el.tagName().toLowerCase();
                if (BLOCK_TAGS.contains(tag)) {
                    sb.append("\n");
                }
            }
        }
    }

    // 공백/개행 정리
    private static String normalize(String s) {
        s = s.replaceAll("[ \\t\\f\\r]+", " ");   // 연속 공백 → 하나
        s = s.replaceAll(" *\\n+ *", "\n");       // 개행 여러 개 → 하나
        return s.trim();
    }
}
