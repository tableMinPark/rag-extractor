package com.extractor.domain.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ToString
public class Passage {

    @Getter
    protected final String docId;

    @Getter
    protected String content;

    @Getter
    protected String subContent;

    @Getter
    protected final int depth;

    protected final int depthSize;

    protected final String[][] titleBuffers;

    public Passage(String docId, int depth, int depthSize) {
        this.docId = docId;
        this.content = "";
        this.subContent = "";
        this.depth = depth;
        this.depthSize = depthSize;
        this.titleBuffers = new String[depthSize][];
    }

    public Passage(String docId, int depth, int depthSize, String[][] titleBuffers) {
        this.docId = docId;
        this.content = "";
        this.subContent = "";
        this.depth = depth;
        this.depthSize = depthSize;
        this.titleBuffers = titleBuffers;
    }

    /**
     * 패시지 셀렉트 (호출)
     */
    public List<Passage> chunk() {
        return Collections.emptyList();
    }

    /**
     * 타이틀 버퍼 정리
     *
     * @param depth       현재 depth
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
     * 토큰 사이즈 조회
     *
     * @return 토큰 사이즈
     */
    public int getTokenSize() {
        return this.content.length() + this.subContent.length();
    }

    /**
     * 전체 타이틀 조회
     *
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
        return fullTitleBuilder.toString().trim();
    }

    /**
     * 타이틀 배열 조회
     *
     * @return 타이틀 배열
     */
    public String[] getTitles() {
        String[] titles = new String[this.titleBuffers.length];
        Arrays.fill(titles, "");

        for (int depth = 0; depth < titleBuffers.length; depth++) {
            StringBuilder titleBuilder = new StringBuilder();

            for (String titleBuffer : titleBuffers[depth]) {
                if (!titleBuffer.isBlank()) {
                    titleBuilder.append(" ").append(titleBuffer);
                }
            }

            // 타이틀 추가
            if (!titleBuilder.isEmpty()) {
                titles[depth] += " " + titleBuilder;
                titles[depth] = titles[depth].trim();
            }
        }

        return titles;
    }

    /**
     * 타이틀 버퍼 배열 깊은 복사
     *
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
