package com.extractor.domain.model.pattern;

import com.extractor.domain.model.PassageDocument;
import com.extractor.domain.vo.pattern.PatternVo;
import com.extractor.domain.vo.pattern.PrefixVo;
import lombok.Builder;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 한줄 단위 패턴 & 토큰 수 기반 청킹
 */
@ToString
public class PatternPassageDocument extends PassageDocument {

    private List<DocumentLine> lines;

    public PatternPassageDocument(String docId, int depthSize, List<DocumentLine> lines) {
        super(docId, -1, depthSize, "", new String[depthSize], "", 0, new String[depthSize][]);
        this.lines = lines;
    }

    @Builder
    public PatternPassageDocument(String docId, int depthSize, List<DocumentLine> lines, int depth, String[][] titleBuffers) {
        super(docId, depth, depthSize, "", new String[depthSize], "", 0, deepCopyTitleBuffers(titleBuffers));
        this.lines = lines;
    }

    /**
     * 패시지 셀렉트
     */
    public List<PatternPassageDocument> chunk(List<PatternVo> patterns, List<String> stopPatterns) {
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
    private static List<PatternPassageDocument> chunk(List<PatternVo> patterns, PatternPassageDocument passage) {
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

        List<PatternPassageDocument> passages = new ArrayList<>();

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
                    passages.addAll(chunk(patterns, PatternPassageDocument.builder()
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

        passages.addAll(chunk(patterns, PatternPassageDocument.builder()
                .docId(passage.docId)
                .depthSize(passage.depthSize)
                .lines(passage.lines.subList(Math.max(head, 0), passage.lines.size()))
                .depth(nextDepth)
                .titleBuffers(deepCopyTitleBuffers(passage.titleBuffers))
                .build()));

        return passages;
    }

    /**
     * 본문 조회
     * @return 본문 문자열
     */
    @Override
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
