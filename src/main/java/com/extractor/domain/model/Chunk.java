package com.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@ToString
public class Chunk {

    public static final String TITLE_PREFIX = " >> ";

    @Getter
    @AllArgsConstructor
    public static class ChunkTitle {
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

    protected final ChunkTitle[][] titleBuffers;

    public Chunk(int depth, int depthSize) {
        this.content = "";
        this.subContent = "";
        this.depth = depth;
        this.depthSize = depthSize;
        this.titleBuffers = new ChunkTitle[depthSize][];
    }

    public Chunk(int depth, int depthSize, ChunkTitle[][] titleBuffers) {
        this.content = "";
        this.subContent = "";
        this.depth = depth;
        this.depthSize = depthSize;
        this.titleBuffers = titleBuffers;
    }

    /**
     * 청킹 (호출)
     */
    public List<Chunk> chunking() {
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
        Queue<ChunkTitle> chunkTitleQueue = new ArrayDeque<>();
        for (ChunkTitle[] titleBuffer : this.titleBuffers) {
            for (ChunkTitle title : titleBuffer) {
                if (title != null) {
                    chunkTitleQueue.offer(title);
                }
            }
        }

        StringBuilder fullTitleBuilder = new StringBuilder();
        while (!chunkTitleQueue.isEmpty()) {
            ChunkTitle chunkTitle = chunkTitleQueue.poll();

            if (!fullTitleBuilder.isEmpty()) {
                fullTitleBuilder.append(TITLE_PREFIX);
            }

            if (chunkTitleQueue.isEmpty()) {
                fullTitleBuilder.append(chunkTitle.title);
            } else {
                fullTitleBuilder.append(chunkTitle.simpleTitle);
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
            Queue<ChunkTitle> chunkTitleQueue = new ArrayDeque<>();

            for (ChunkTitle title : titleBuffers[depth]) {
                if (title != null) {
                    chunkTitleQueue.offer(title);
                }
            }

            StringBuilder titleBuilder = new StringBuilder();
            while (!chunkTitleQueue.isEmpty()) {
                ChunkTitle chunkTitle = chunkTitleQueue.poll();

                if (!titleBuilder.isEmpty()) {
                    titleBuilder.append(TITLE_PREFIX);
                }

                if (chunkTitleQueue.isEmpty()) {
                    titleBuilder.append(chunkTitle.title);
                } else {
                    titleBuilder.append(chunkTitle.simpleTitle);
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
    protected static ChunkTitle[][] deepCopyTitleBuffers(ChunkTitle[][] titleBuffers) {
        ChunkTitle[][] titleBuffersCopy = new ChunkTitle[titleBuffers.length][];
        for (int depth = 0; depth < titleBuffers.length; depth++) {
            titleBuffersCopy[depth] = new ChunkTitle[titleBuffers[depth].length];
            for (int titleIndex = 0; titleIndex < titleBuffers[depth].length; titleIndex++) {
                if (titleBuffers[depth][titleIndex] != null) {
                    titleBuffersCopy[depth][titleIndex] = new ChunkTitle(
                            titleBuffers[depth][titleIndex].title,
                            titleBuffers[depth][titleIndex].simpleTitle);
                }
            }
        }
        return titleBuffersCopy;
    }
}