package com.extractor.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@ToString
@Getter
public class PassageDocument {

    private final String docId;

    private final int num;

    private String fullTitle;

    private String[] titles;

    private String content;

    private int tokenSize;

    @JsonIgnore
    private final String[][] titleBuffers;

    @JsonIgnore
    private final List<DocumentLine> lines;

    @Builder
    public PassageDocument(String docId, int num, String[][] titleBuffers, List<DocumentLine> lines) {
        this.docId = docId;
        this.num = num;
        this.fullTitle = "";
        this.titles = new String[titleBuffers.length];
        this.content = "";
        this.titleBuffers = new String[titleBuffers.length][];
        this.lines = lines;

        for (int titleBufferIndex = 0; titleBufferIndex < titleBuffers.length; titleBufferIndex++) {
            this.titleBuffers[titleBufferIndex] = Arrays.copyOf(titleBuffers[titleBufferIndex], titleBuffers[titleBufferIndex].length);
        }
    }

//    /**
//     * DFS 청킹
//     */
//    public List<PassageDocument> chunk() {
//
//    }

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
     * 본문 조회
     * @return 본문 문자열
     */
    public String getContent() {
        StringBuilder contentBuilder = new StringBuilder();
        for (DocumentLine line : this.lines) {
            String prefix = line.getPrefix();
            String content = line.getContent().trim();

            if (!prefix.isBlank()) {
                content = content.replaceFirst(prefix, "");
            }

            contentBuilder.append("\n").append(content.trim());
        }
        return this.content = contentBuilder.toString().trim();
    }

//    /**
//     * 토큰 사이즈 조회
//     * @return 토큰 사이즈
//     */
//    public int getTokenSize() {
//
//    }
}
