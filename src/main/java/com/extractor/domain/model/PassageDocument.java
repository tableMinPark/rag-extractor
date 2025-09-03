package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
@Getter
public class PassageDocument {

    private final String docId;

    private final int num;

    private String fullTitle;

    private final String[] titles;

    private String content;

    private int tokenSize;

    @Builder
    public PassageDocument(String docId, int num, int depthSize) {
        this.docId = docId;
        this.num = num;
        this.titles = new String[depthSize];
        this.tokenSize = 0;
        Arrays.fill(this.titles, "");
    }

    /**
     * 패시지 문서 생성
     * @param docId 추출 문서 식별자
     * @param num 번호
     * @param depthSize 최대 깊이
     * @param titleBuffers 타이틀 버퍼 목록
     * @param contentBuffer 본문 버퍼
     * @return 패시지 문서
     */
    public static PassageDocument of(String docId, int num, int depthSize, String[][] titleBuffers, StringBuilder contentBuffer) {

        // 패시지 문서 생성
        PassageDocument passageDocument = PassageDocument.builder()
                .docId(docId)
                .num(num)
                .depthSize(depthSize)
                .build();

        // 타이틀 생성
        StringBuilder fullTitleBuilder = new StringBuilder();
        for (int depth = 0; depth < titleBuffers.length; depth++) {
            StringBuilder titleBuilder = new StringBuilder();
            for (String titleBuffer : titleBuffers[depth]) {
                if (!titleBuffer.isBlank()) {
                    titleBuilder.append(" ").append(titleBuffer);
                }
            }
            // 타이틀 추가
            if (!titleBuilder.isEmpty()) {
                fullTitleBuilder.append(titleBuilder);
                passageDocument.addTitle(depth, titleBuilder.toString());
            }
        }

        // 전체 타이틀 설정
        passageDocument.setFullTitle(fullTitleBuilder.toString());
        // 본문 추가
        passageDocument.setContent(contentBuffer.toString());

        return passageDocument;
    }

    /**
     * 필드 데이터 추가
     */
    public void addTitle(int depth, String title) {
        if (depth < this.titles.length) {
            this.titles[depth] += " " + title;
            this.titles[depth] = this.titles[depth].trim();
        }
    }

    /**
     * 본문 설정
     * @param content 본문
     */
    public void setContent(String content) {
        this.content = content.trim();
        this.tokenSize = this.content.length();
    }

    /**
     * 전체 타이틀 설정
     * @param fullTitle 전체 타이틀
     */
    public void setFullTitle(String fullTitle) {
        this.fullTitle = fullTitle.trim();
    }
}
