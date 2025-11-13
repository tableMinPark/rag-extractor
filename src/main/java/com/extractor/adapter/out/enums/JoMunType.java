package com.extractor.adapter.out.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public enum JoMunType {

    PYUN   ("전문", "편", "^제\\d+편"),
    JANG   ("전문", "장", "^제\\d+장"),
    JUL    ("전문", "절", "^제\\d+절"),
    GWAN   ("전문", "관", "^제\\d+편"),
    JO     ("조문", "조", "(^제\\d+조의\\d+)|(^제\\d+조)"),
    UNKNOWN("", "", ""),
    ;

    private final String type;

    private final String description;

    private final String pattern;

    public boolean isJunMun() {
        return "전문".equals(this.type);
    }

    /**
     * 조문 여부 일치 & 조문 내용 정규식 검증
     * @param type 조문 여부
     * @param content 조문 내용
     * @return JoMunType enum
     */
    public static JoMunType getType(String type, String content) {
        return Arrays.stream(values())
                .filter(joMunType -> joMunType.type.equals(type))
                .filter(joMunType -> Pattern.compile(joMunType.pattern).matcher(content).find())
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static List<JoMunType> getJunMunTypes() {
        return  Arrays.stream(JoMunType.values())
                .filter(JoMunType::isJunMun)
                .toList();
    }
}
