package com.extractor.global.utils;

import java.util.List;
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
     * 공백/개행 정리
     *
     * @param str 원본 문자열
     * @return 공백 정리 문자열
     */
    public static String normalize(String str) {
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