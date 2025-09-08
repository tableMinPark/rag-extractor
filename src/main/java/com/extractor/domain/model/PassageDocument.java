package com.extractor.domain.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
public class PassageDocument {

    @Getter
    protected final String docId;

    protected int tokenSize;

    protected String fullTitle;

    protected String[] titles;

    @Getter
    protected String content;

    @Getter
    protected final int depth;

    protected final int depthSize;

    protected final String[][] titleBuffers;

    public PassageDocument(String docId, int depth, int depthSize, String content, String[] titles, String fullTitle, int tokenSize, String[][] titleBuffers) {
        this.docId = docId;
        this.depth = depth;
        this.depthSize = depthSize;
        this.content = content;
        this.titles = titles;
        this.fullTitle = fullTitle;
        this.tokenSize = tokenSize;
        this.titleBuffers = titleBuffers;
    }

    /**
     * 타이틀 버퍼 정리
     * @param depth 현재 depth
     * @param prefixIndex 현재 prefixIndex
     */
    protected void titleBufferClear(int depth, int prefixIndex) {
        for (int nowDepth = depth; nowDepth < this.depthSize; nowDepth++) {
            int nowPrefixIndex = nowDepth != depth ? 0 : prefixIndex;

            while (nowPrefixIndex < this.titleBuffers[nowDepth].length) {
                this.titleBuffers[nowDepth][nowPrefixIndex] = "";
                nowPrefixIndex++;
            }
        }
    }

    /**
     * 타이틀 배열 조회
     * @return 타이틀 배열
     */
    public String[] getTitles() {

        this.titles = new String[this.titleBuffers.length];
        Arrays.fill(this.titles, "");

        for (int depth = 0; depth < titleBuffers.length; depth++) {
            StringBuilder titleBuilder = new StringBuilder();
            for (String titleBuffer : titleBuffers[depth]) {
                if (!titleBuffer.isBlank()) {
                    titleBuilder.append(" ").append(titleBuffer);
                }
            }
            // 타이틀 추가
            if (!titleBuilder.isEmpty()) {
                this.titles[depth] += " " + titleBuilder;
                this.titles[depth] = this.titles[depth].trim();
            }
        }

        return this.titles;
    }

    /**
     * 전체 타이틀 조회
     * @return 전체 타이틀 문자열
     */
    public String getFullTitle() {
        StringBuilder fullTitleBuilder = new StringBuilder();
        for (String[] buffer : this.titleBuffers) {
            StringBuilder titleBuilder = new StringBuilder();
            for (String titleBuffer : buffer) {
                if (!titleBuffer.isBlank()) {
                    titleBuilder.append(" ").append(titleBuffer);
                }
            }
            // 타이틀 추가
            if (!titleBuilder.isEmpty()) {
                fullTitleBuilder.append(titleBuilder);
            }
        }
        return this.fullTitle = fullTitleBuilder.toString().trim();
    }

    /**
     * 토큰 사이즈 조회
     * @return 토큰 사이즈
     */
    public int getTokenSize() {
        String content = this.getContent();
        return this.tokenSize = content.length();
    }

    /**
     * 타이틀 버퍼 배열 깊은 복사
     * @param titleBuffers 원천
     * @return 복사 배열
     */
    protected static String[][] deepCopyTitleBuffers(String[][] titleBuffers) {
        String[][] titleBuffersCopy = new String[titleBuffers.length][];
        for (int titleBufferIndex = 0; titleBufferIndex < titleBuffers.length; titleBufferIndex++) {
            titleBuffersCopy[titleBufferIndex] = Arrays.copyOf(titleBuffers[titleBufferIndex], titleBuffers[titleBufferIndex].length);
        }
        return titleBuffersCopy;
    }
}
