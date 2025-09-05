package com.extractor.domain.model;

import com.extractor.domain.vo.pattern.PatternVo;
import com.extractor.domain.vo.pattern.PrefixVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Getter
public class PassageDocument {

    private final String docId;

    @JsonIgnore
    private final int depthSize;

    @JsonIgnore
    private List<DocumentLine> lines;

    private int tokenSize;

    private String fullTitle;

    private String[] titles;

    private String content;

    private final int depth;

    @JsonIgnore
    private final String[][] titleBuffers;

    public PassageDocument(String docId, int depthSize, List<DocumentLine> lines) {
        this.docId = docId;
        this.depthSize = depthSize;
        this.lines = lines;

        this.fullTitle = "";
        this.titles = new String[this.depthSize];
        this.content = "";
        this.depth = -1;

        this.titleBuffers = new String[this.depthSize][];
    }

    @Builder
    public PassageDocument(String docId, int depthSize, List<DocumentLine> lines, int depth, String[][] titleBuffers) {
        this.docId = docId;
        this.depthSize = depthSize;
        this.lines = lines;

        this.fullTitle = "";
        this.titles = new String[this.depthSize];
        this.content = "";
        this.depth = depth;

        this.titleBuffers = deepCopyTitleBuffers(titleBuffers);
    }

    /**
     * 타이틀 버퍼 정리
     * @param depth 현재 depth
     * @param prefixIndex 현재 prefixIndex
     */
    private void titleBufferClear(int depth, int prefixIndex) {
        for (int nowDepth = depth; nowDepth < this.depthSize; nowDepth++) {
            int nowPrefixIndex = nowDepth != depth ? 0 : prefixIndex;

            while (nowPrefixIndex < this.titleBuffers[nowDepth].length) {
                this.titleBuffers[nowDepth][nowPrefixIndex] = "";
                nowPrefixIndex++;
            }
        }
    }

    /**
     * 패시지 셀렉트
     */
    public List<PassageDocument> chunk(List<PatternVo> patterns, List<String> stopPatterns) {
        // 타이틀 버퍼 초기화
        for (int depth = 0; depth < this.depthSize; depth++) {
            int titleBufferSize = patterns.get(depth).getPrefixes().size();
            this.titleBuffers[depth] = new String[titleBufferSize];
            Arrays.fill(this.titleBuffers[depth], "");
        }

        int head = 0, tail = 0;

        List<String> prefixes = new ArrayList<>();
        for (PatternVo pattern : patterns) prefixes.addAll(pattern.getPrefixes().stream().map(PrefixVo::getPrefix).toList());

        while (tail < this.lines.size()) {
            DocumentLine line = this.lines.get(tail++);
            // 중단 조건 확인
            if (isPatternMatch(line, stopPatterns)) break;
            // 일치 조건 확인
            if (head == 0 && isPatternMatch(line, prefixes)) {
                head = tail - 1;
            }
        }

        this.lines = this.lines.subList(head, tail);

        return chunk(patterns, this);
    }

    /**
     * 패시지 셀력트 재귀 함수
     * @param patterns 패턴
     * @param passage 패시지 문서
     * @return 샐렉팅 패시지 문서 목록
     */
    private static List<PassageDocument> chunk(List<PatternVo> patterns, PassageDocument passage) {
        int nextDepth = passage.depth + 1;

        // 라인 없음
        if (passage.lines.isEmpty()) return Collections.emptyList();
        // 뎁스 초과
        if (nextDepth >= passage.depthSize) {
            return passage.getTokenSize() == 0
                    ? Collections.emptyList()
                    : List.of(passage);
        }
        // 토큰 수 적합
        if (passage.getTokenSize() <= patterns.get(nextDepth).getTokenSize()) {
            return passage.getTokenSize() == 0
                    ? Collections.emptyList()
                    : List.of(passage);
        }

        List<PassageDocument> passages = new ArrayList<>();

        int head = -1;
        PatternVo patternVo = patterns.get(nextDepth);
        for (int lineIndex = 0; lineIndex < passage.lines.size(); lineIndex++) {
            DocumentLine line = passage.lines.get(lineIndex);
            for (int prefixIndex = 0; prefixIndex < patternVo.getPrefixes().size(); prefixIndex++) {
                PrefixVo prefix = patternVo.getPrefixes().get(prefixIndex);
                Pattern pattern = Pattern.compile(prefix.getPrefix(), Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(line.getContent());

                if (matcher.find()) {
                    // 정규식 설정
                    line.setPrefix(prefix.getPrefix());
                    // 재귀
                    passages.addAll(chunk(patterns, PassageDocument.builder()
                            .docId(passage.docId)
                            .depthSize(passage.depthSize)
                            .lines(passage.lines.subList(Math.max(head, 0), lineIndex))
                            .depth(nextDepth)
                            .titleBuffers(deepCopyTitleBuffers(passage.titleBuffers))
                            .build()));
                    head = lineIndex;
                    // 타이틀 버퍼 정리
                    passage.titleBufferClear(nextDepth, prefixIndex);
                    // 타이틀 지정

                    if (!prefix.getIsDeleting()) {
                        passage.titleBuffers[nextDepth][prefixIndex] = matcher.group().trim();
                    }
                    break;
                }
            }
        }

        passages.addAll(chunk(patterns, PassageDocument.builder()
                .docId(passage.docId)
                .depthSize(passage.depthSize)
                .lines(passage.lines.subList(Math.max(head, 0), passage.lines.size()))
                .depth(nextDepth)
                .titleBuffers(deepCopyTitleBuffers(passage.titleBuffers))
                .build()));

        return passages;
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
     * 본문 조회
     * @return 본문 문자열
     */
    public String getContent() {

        if (!this.lines.isEmpty()) {
            StringBuilder contentBuilder = new StringBuilder(this.lines.getFirst().getSimpleContent());
            for (int lineIndex = 1; lineIndex < this.lines.size(); lineIndex++) {
                DocumentLine line = this.lines.get(lineIndex);
                String content = line.getContent().trim();
                contentBuilder.append("\n").append(content.trim());
            }
            this.content = contentBuilder.toString().trim();
        }
        return this.content;
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
    private static String[][] deepCopyTitleBuffers(String[][] titleBuffers) {
        String[][] titleBuffersCopy = new String[titleBuffers.length][];
        for (int titleBufferIndex = 0; titleBufferIndex < titleBuffers.length; titleBufferIndex++) {
            titleBuffersCopy[titleBufferIndex] = Arrays.copyOf(titleBuffers[titleBufferIndex], titleBuffers[titleBufferIndex].length);
        }
        return titleBuffersCopy;
    }

    /**
     * 패턴 확인
     * @param line 문서 라인
     * @param prefixes 패턴 목록
     * @return 패턴 일치 여부
     */
    private static boolean isPatternMatch(DocumentLine line, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (isPatternMatch(line, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 패턴 확인
     * @param line 문서 라인
     * @param prefix 정규식
     * @return 패턴 일치 여부
     */
    private static boolean isPatternMatch(DocumentLine line, String prefix) {
        Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(line.getContent());
        return matcher.find();
    }
}
