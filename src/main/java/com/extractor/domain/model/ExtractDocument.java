package com.extractor.domain.model;

import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.domain.vo.pattern.PatternVo;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ToString
@Getter
public class ExtractDocument {

    private final String docId;

    private final String name;

    private final Path path;

    private final List<DocumentLine> lines;

    private final List<PassageDocument> passages;

    // 버퍼
    private int depthSize;
    private List<PatternVo> patterns;
    private List<String> stopPatterns;
    private String[][] titleBuffers;
    private List<DocumentLine> contentBuffer;

    public ExtractDocument(String docId, String name, Path path) {
        this.docId = docId;
        this.name = name;
        this.path = path;
        this.lines = new ArrayList<>();
        this.passages = new ArrayList<>();
    }

    /**
     * 유효 라인 범위 확인
     * @return 유효 라인 범위 (0: head, 1: tail)
     */
    private int[] getLineArrange() {
        int head = 0, tail = 0;

        List<String> prefixes = new ArrayList<>();

        for (PatternVo pattern : this.patterns) {
            prefixes.addAll(pattern.getPrefixes());
        }

        while (tail < this.lines.size()) {
            DocumentLine line = this.lines.get(tail++);
            // 중단 조건 확인
            if (isPatternMatch(line, this.stopPatterns)) break;
            // 일치 조건 확인
            if (isPatternMatch(line, prefixes) && head == 0) {
                head = tail - 1;
            }
        }

        return new int[]{head, tail};
    }

    /**
     * 버퍼 초기화
     * @param chunkPatternVo 분류 패턴
     */
    private void resetBuffer(ChunkPatternVo chunkPatternVo) {
        // 기존 패시지 초기화
        this.passages.clear();
        this.depthSize = chunkPatternVo.getPatterns().size();
        this.patterns = chunkPatternVo.getPatterns();
        this.stopPatterns = chunkPatternVo.getStopPatterns();
        this.contentBuffer = new ArrayList<>();

        this.titleBuffers = new String[this.depthSize][];
        for (int depth = 0; depth < depthSize; depth++) {
            int titleBufferSize = this.patterns.get(depth).getPrefixes().size();
            this.titleBuffers[depth] = new String[titleBufferSize];
            Arrays.fill(this.titleBuffers[depth], "");
        }
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
     * 타이틀 설정
     * @param depth 현재 depth
     * @param prefixIndex 현재 prefixIndex
     */
    private void setTitleBuffer(int depth, int prefixIndex, String title) {
        this.titleBuffers[depth][prefixIndex] = title;
    }

    /**
     * 본문 버퍼 플러시
     */
    private void contentBufferFlush() {
        if (!this.contentBuffer.toString().trim().isBlank()) {
            this.passages.add(new PassageDocument(this.docId, this.passages.size(), this.titleBuffers, this.contentBuffer));

            // 본문 버퍼 초기화
            this.contentBuffer = new ArrayList<>();
        }
    }

    /**
     * 상위 -> 하위 순차 패시지 분류
     * @param chunkPatternVo 분류 패턴
     */
    public void topDownSelectPassage(ChunkPatternVo chunkPatternVo) {
        // 초기화
        this.resetBuffer(chunkPatternVo);

        int[] lineArrange = this.getLineArrange();
        int head = lineArrange[0];
        int tail = lineArrange[1];

        for (int lineIndex = head; lineIndex < tail; lineIndex++) {
            DocumentLine line = this.lines.get(lineIndex);

            switch (line.getType()) {
                case DocumentLine.LineType.TEXT -> {
                    boolean isMatched = false;

                    for (int depth = 0; depth < this.patterns.size(); depth++) {
                        PatternVo patternVo = this.patterns.get(depth);

                        for (int prefixIndex = 0; prefixIndex < patternVo.getPrefixes().size(); prefixIndex++) {
                            String prefix = patternVo.getPrefixes().get(prefixIndex);
                            Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
                            Matcher matcher = pattern.matcher(line.getContent());

                            if (matcher.find()) {
                                line.setPrefix(prefix);

                                // 본문 버퍼 플러시
                                this.contentBufferFlush();

                                // 타이틀 버퍼 정리
                                this.titleBufferClear(depth, prefixIndex);

                                // 타이틀 저장
                                this.setTitleBuffer(depth, prefixIndex, matcher.group().trim());

                                // 타이틀 내 본문 내용 확인
                                String contentInTitle = line.getContent().replaceFirst(prefix, "").trim();

                                // 타이틀 라인에 본문 내용이 있는 경우 본문 버퍼에 포함
                                if (!contentInTitle.isBlank()) this.contentBuffer.add(line);

                                // 매칭
                                isMatched = true;
                                break;
                            }
                        }
                    }


                    if (!isMatched) this.contentBuffer.add(line);
                }
                case DocumentLine.LineType.TABLE, DocumentLine.LineType.IMAGE -> this.contentBuffer.add(line);
            }
        }

        // 본문 버퍼 플러시
        this.contentBufferFlush();
    }

    /**
     * TODO: 묶음 단위 패시지 처리
     * TODO: 패턴 마다 토큰 수 제약을 걸 수 있도록 변경 필요 | ex) "절 단위 하위로는 토큰 제약 걸지 않음" 과 같은 제약 필요
     * ex)
     * 1, 장 단위 묶음 처리
     * 2. 장 단위 중 2000자 넘는 묶음만 절 단위 묶음 처리
     * 3. 절 단위 중 2000자 넘는 묶음만 조 단위 묶음 처리
     * ...
     * ...
     */
    public void bundleSelectPassage(ChunkPatternVo chunkPatternVo) {
    }

    /**
     * 문자 데이터 등록
     */
    public void addText(String text) {
        String content = text.trim();
        int sum = this.lines.isEmpty()
                ? content.length()
                : this.lines.getLast().getSum() + content.length();

        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TEXT)
                    .content(content)
                    .sum(sum)
                    .build());
        }
    }

    /**
     * 표 데이터 등록
     */
    public void addTable(String table) {
        String content = table.trim();
        int sum = this.lines.isEmpty()
                ? content.length()
                : this.lines.getLast().getSum() + content.length();

        if (!table.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TABLE)
                    .content(content)
                    .sum(sum)
                    .build());
        }
    }

    /**
     * 이미지 데이터 등록
     */
    public void addImage(String text) {
        String content = text.trim();
        int sum = this.lines.isEmpty()
                ? content.length()
                : this.lines.getLast().getSum() + content.length();

        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.IMAGE)
                    .content(content)
                    .sum(sum)
                    .build());
        }
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
