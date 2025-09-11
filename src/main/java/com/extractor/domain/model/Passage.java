package com.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@ToString
public class Passage {

    @Getter
    @AllArgsConstructor
    public static class PassageTitle {
        private String title;
        private String simpleTitle;
    }

    @Getter
    protected String content;

    @Getter
    protected String subContent;

    @Getter
    protected final int depth;

    protected final int depthSize;

    protected final PassageTitle[][] titleBuffers;

    public Passage(int depth, int depthSize) {
        this.content = "";
        this.subContent = "";
        this.depth = depth;
        this.depthSize = depthSize;
        this.titleBuffers = new PassageTitle[depthSize][];
    }

    public Passage(int depth, int depthSize, PassageTitle[][] titleBuffers) {
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
                this.titleBuffers[nowDepth][nowPrefixIndex] = null;
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
        Queue<PassageTitle> passageTitleQueue = new ArrayDeque<>();
        for (PassageTitle[] titleBuffer : this.titleBuffers) {
            for (PassageTitle title : titleBuffer) {
                if (title != null) {
                    passageTitleQueue.offer(title);
                }
            }
        }

        StringBuilder fullTitleBuilder = new StringBuilder();
        while (!passageTitleQueue.isEmpty()) {
            PassageTitle passageTitle = passageTitleQueue.poll();

            if (!fullTitleBuilder.isEmpty()) {
                fullTitleBuilder.append(" | ");
            }

            if (passageTitleQueue.isEmpty()) {
                fullTitleBuilder.append(passageTitle.title);
            } else {
                fullTitleBuilder.append(passageTitle.simpleTitle);
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
            Queue<PassageTitle> passageTitleQueue = new ArrayDeque<>();

            for (PassageTitle title : titleBuffers[depth]) {
                if (title != null) {
                    passageTitleQueue.offer(title);
                }
            }

            StringBuilder titleBuilder = new StringBuilder();
            while (!passageTitleQueue.isEmpty()) {
                PassageTitle passageTitle = passageTitleQueue.poll();

                if (!titleBuilder.isEmpty()) {
                    titleBuilder.append(" | ");
                }

                if (passageTitleQueue.isEmpty()) {
                    titleBuilder.append(passageTitle.title);
                } else {
                    titleBuilder.append(passageTitle.simpleTitle);
                }
            }

            // 타이틀 추가
            titles[depth] = titleBuilder.toString().trim();
        }

        return titles;
    }

    /**
     * 타이틀 버퍼 배열 깊은 복사
     *
     * @param titleBuffers 원천
     * @return 복사 배열
     */
    protected static PassageTitle[][] deepCopyTitleBuffers(PassageTitle[][] titleBuffers) {
        PassageTitle[][] titleBuffersCopy = new PassageTitle[titleBuffers.length][];
        for (int depth = 0; depth < titleBuffers.length; depth++) {
            titleBuffersCopy[depth] = new PassageTitle[titleBuffers[depth].length];
            for (int titleIndex = 0; titleIndex < titleBuffers[depth].length; titleIndex++) {
                if (titleBuffers[depth][titleIndex] != null) {
                    titleBuffersCopy[depth][titleIndex] = new PassageTitle(
                            titleBuffers[depth][titleIndex].title,
                            titleBuffers[depth][titleIndex].simpleTitle);
                }
            }
        }
        return titleBuffersCopy;
    }
}