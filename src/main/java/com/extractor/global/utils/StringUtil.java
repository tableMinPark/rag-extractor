package com.extractor.global.utils;

import java.util.UUID;

public class StringUtil {

    /**
     * 랜덤 ID 값 생성
     * @return 랜덤 ID
     */
    public static String generateRandomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
