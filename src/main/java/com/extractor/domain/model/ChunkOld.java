package com.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@ToString
public class ChunkOld {

    public static final String TITLE_PREFIX = " >> ";
    protected static final String[] TOKEN_CHUNKING_PREFIXES = { "(?<=\n\n)", "(?<=\n)" };

    @Getter
    @AllArgsConstructor
    public static class ChunkTitle {
        private String title;
        private String simpleTitle;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    protected static class ChunkTokenContent {
        private int depth;
        private String content;
    }

    @Getter
    protected String content;

    @Getter
    protected String subContent;

    @Getter
    protected int tokenSize;

    protected int overlapSize;

    @Getter
    protected final int depth;

    protected final int depthSize;

    protected final int maxTokenSize;

    protected final ChunkTitle[][] titleBuffers;

    protected ChunkOld(int depth, int depthSize, int maxTokenSize, int overlapSize) {
        this.content = "";
        this.subContent = "";
        this.tokenSize = 0;
        this.overlapSize = overlapSize;
        this.depth = depth;
        this.depthSize = depthSize;
        this.maxTokenSize = maxTokenSize;
        this.titleBuffers = new ChunkTitle[depthSize][];
    }

    protected ChunkOld(int depth, int depthSize, int maxTokenSize, int overlapSize, ChunkTitle[][] titleBuffers) {
        this.content = "";
        this.subContent = "";
        this.tokenSize = 0;
        this.overlapSize = overlapSize;
        this.depth = depth;
        this.depthSize = depthSize;
        this.maxTokenSize = maxTokenSize;
        this.titleBuffers = titleBuffers;
    }

    protected ChunkOld(int depth, int depthSize, int maxTokenSize, int overlapSize, ChunkTitle[][] titleBuffers, String content, String subContent) {
        this.content = content;
        this.subContent = subContent;
        this.tokenSize = content.length() + subContent.length();
        this.overlapSize = overlapSize;
        this.depth = depth;
        this.depthSize = depthSize;
        this.maxTokenSize = maxTokenSize;
        this.titleBuffers = titleBuffers;
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

    /**
     * 청킹 데이터 오버랩 적용
     *
     * @param overlapSize 오버랩 사이즈
     * @param chunks      청킹 데이터 목록
     */
    protected static void setOverlap(int overlapSize, List<ChunkOld> chunks) {

        List<String> contents = chunks.stream().map(ChunkOld::getContent).toList();

        for (int contentsIndex = 0; contentsIndex < contents.size(); contentsIndex++) {
            String headOverlap = "";
            String tailOverlap = "";

            // 전 오버랩
            if (contentsIndex - 1 >= 0) {
                String headContent = contents.get(contentsIndex - 1);
                headOverlap = headContent.substring(Math.max(0, headContent.length() - overlapSize));
            }
            // 후 오버랩
            if (contentsIndex + 1 < chunks.size()) {
                String tailContent = contents.get(contentsIndex + 1);
                tailOverlap = tailContent.substring(0, Math.min(overlapSize, tailContent.length()));
            }

            String content = contents.get(contentsIndex);
            int tokenSize = chunks.get(contentsIndex).tokenSize;

            chunks.get(contentsIndex).content = headOverlap + content + tailOverlap;
            chunks.get(contentsIndex).tokenSize = tokenSize + headOverlap.length() + tailOverlap.length();
        }
    }
}