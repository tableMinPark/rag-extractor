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
    private List<PatternVo> patterns;
    private List<String> stopPatterns;
    private String[][] titleBuffers;
    private StringBuilder contentBuffer;

    public ExtractDocument(String docId, String name, Path path) {
        this.docId = docId;
        this.name = name;
        this.path = path;
        this.lines = new ArrayList<>();
        this.passages = new ArrayList<>();
    }

    /**
     * 유효 라인 범위 확인
     * @param patterns 허용 패턴
     * @param stopPatterns 중단 패턴
     * @return 유효 라인 범위 (0: head, 1: tail)
     */
    private int[] getLineArrange(List<PatternVo> patterns, List<String> stopPatterns) {

        int head = 0;
        int tail = 0;

        List<String> prefixes = patterns.stream()
                .map(PatternVo::getPrefix)
                .toList();

        while (tail < this.lines.size()) {
            DocumentLine line = this.lines.get(tail++);
            // 중단 조건 확인
            if (isPatternMatch(line, stopPatterns)) break;
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

        this.patterns = chunkPatternVo.getPatterns();
        this.stopPatterns = chunkPatternVo.getStopPatterns();
        this.titleBuffers = new String[chunkPatternVo.getDepthSize()][patterns.size()];
        this.contentBuffer = new StringBuilder();
        for (String[] titleBuffer : this.titleBuffers) Arrays.fill(titleBuffer, "");
    }

    /**
     * 상위 -> 하위 순차 패시지 분류
     * @param chunkPatternVo 분류 패턴
     */
    public void topDownSelectPassage(ChunkPatternVo chunkPatternVo) {
        // 초기화
        this.resetBuffer(chunkPatternVo);

        int[] lineArrange = this.getLineArrange(this.patterns, this.stopPatterns);
        int head = lineArrange[0];
        int tail = lineArrange[1];

        for (int lineIndex = head; lineIndex < tail; lineIndex++) {
            DocumentLine line = this.lines.get(lineIndex);

            switch (line.getType()) {
                case DocumentLine.LineType.TEXT -> {
                    boolean isMatched = false;
                    for (int patternIndex = 0; patternIndex < this.patterns.size(); patternIndex++) {
                        PatternVo patternVo = this.patterns.get(patternIndex);

                        String prefix = patternVo.getPrefix();
                        Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(line.getContent());

                        if (matcher.find()) {
                            // 본문 버퍼 플러시
                            if (!this.contentBuffer.toString().trim().isBlank()) {
                                // 목록 저장
                                PassageDocument passageDocument = PassageDocument.of(
                                        this.docId, this.passages.size(), chunkPatternVo.getDepthSize(), this.titleBuffers, this.contentBuffer);

                                if (!passageDocument.getFullTitle().isBlank()) {
                                    this.passages.add(passageDocument);
                                }

                                // 본문 버퍼 초기화
                                this.contentBuffer = new StringBuilder();
                            }

                            // 타이틀 버퍼 정리
                            for (int depth = patternVo.getDepth(); depth < this.titleBuffers.length; depth++) {
                                int childPatternIndex = depth == patternVo.getDepth() ? patternIndex : 0;

                                while (childPatternIndex < this.titleBuffers[depth].length) {
                                    this.titleBuffers[depth][childPatternIndex] = "";
                                    childPatternIndex++;
                                }
                            }

                            // 타이틀 저장
                            String title = matcher.group().trim();
                            this.titleBuffers[patternVo.getDepth()][patternIndex] = title;
                            String content = line.getContent().replaceFirst(prefix, "").trim();

                            // 타이틀 라인에 본문 내용이 있는 경우
                            if (!content.isBlank()) this.contentBuffer.append("\n").append(content);

                            isMatched = true;
                            line.setPrefix(prefix);
                            break;
                        }
                    }

                    if (!isMatched) this.contentBuffer.append("\n").append(line.getContent());
                }
                case DocumentLine.LineType.TABLE, DocumentLine.LineType.IMAGE -> this.contentBuffer.append("\n").append(line.getContent());
            }
        }

        // 본문 버퍼 플러시
        if (!this.contentBuffer.toString().trim().isBlank()) {
            // 목록 저장
            PassageDocument passageDocument = PassageDocument.of(
                    this.docId, this.passages.size(), chunkPatternVo.getDepthSize(), this.titleBuffers, this.contentBuffer);
            if (!passageDocument.getFullTitle().isBlank()) {
                this.passages.add(passageDocument);
            }
        }
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
        // 초기화
        this.resetBuffer(chunkPatternVo);

        int[] lineArrange = this.getLineArrange(this.patterns, this.stopPatterns);
        int head = lineArrange[0];
        int tail = lineArrange[1];

        this.bundleSelectPassage(head, tail, 0, chunkPatternVo);
    }

    /**
     * 묶음 단위 패시지 처리 재귀 함수
     * @param head 라인 시작점
     * @param tail 라인 종료점
     * @param depth 깊이
     * @param chunkPatternVo 패시지 패턴
     */
    private void bundleSelectPassage(int head, int tail, int depth, ChunkPatternVo chunkPatternVo) {

        int tokenSize = head == 0
                ? this.lines.get(tail - 1).getSum()
                : this.lines.get(tail - 1).getSum() - this.lines.get(head - 1).getSum();
        List<PatternVo> patterns = this.patterns.stream()
                .filter(patternVo -> patternVo.getDepth() == depth)
                .toList();

        // TODO: PatternVo 에서 최대 토큰 수를 받아오도록 변경 필요
        if (tokenSize <= chunkPatternVo.getTokenSize()) {
            // TODO: 테스트 출력
            System.out.printf("토큰수 적합 >> [%d] <%2d <-> %-2d> (%d) %s%n", depth, head, tail, tokenSize, this.lines.get(head).getContent());
        } else if (patterns.isEmpty()) {
            // TODO: 테스트 출력
            System.out.printf("마지막 패턴 >> [%d] <%2d <-> %-2d> (%d) %s%n", depth, head, tail, tokenSize, this.lines.get(head).getContent());
        } else {
            // 가능 토큰 수 초과
            int nowHead = head;
            for (int lineIndex = head + 1; lineIndex < tail; lineIndex++) {
                DocumentLine line = this.lines.get(lineIndex);

                for (PatternVo pattern : patterns) {
                    if (isPatternMatch(line, pattern.getPrefix())) {
                        this.bundleSelectPassage(nowHead, lineIndex, depth + 1, chunkPatternVo);
                        nowHead = lineIndex;
                        break;
                    }
                }
            }

            this.bundleSelectPassage(nowHead, tail, depth + 1, chunkPatternVo);
        }
    }

    /**
     * 문자 데이터 등록
     */
    public void addText(String text) {
        int sum = this.lines.isEmpty()
                ? text.length()
                : this.lines.getLast().getSum() + text.length();

        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TEXT)
                    .content(text)
                    .sum(sum)
                    .build());
        }
    }

    /**
     * 표 데이터 등록
     */
    public void addTable(String table) {
        int sum = this.lines.isEmpty()
                ? table.length()
                : this.lines.getLast().getSum() + table.length();

        if (!table.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TABLE)
                    .content(table)
                    .sum(sum)
                    .build());
        }
    }

    /**
     * 이미지 데이터 등록
     */
    public void addImage(String text) {
        int sum = this.lines.isEmpty()
                ? text.length()
                : this.lines.getLast().getSum() + text.length();

        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.IMAGE)
                    .content(text)
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
