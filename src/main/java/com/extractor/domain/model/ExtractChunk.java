package com.extractor.domain.model;

import com.extractor.domain.processor.ChunkProcessor;
import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
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
public class ExtractChunk extends Chunk implements ChunkProcessor {

    private final List<ExtractContent> extractContents;

    private final List<PatternVo> patterns;

    private final List<String> stopPatterns;

    /**
     * 루트 청크 생성자
     *
     * @param extractContents 추출 본문 목록
     * @param patterns        청크 패턴
     * @param stopPatterns    정지 패턴
     */
    public ExtractChunk(List<ExtractContent> extractContents, List<PatternVo> patterns, List<String> stopPatterns, int maxTokenSize, int maxOverlapSize) {
        super(-1, patterns.size(), maxTokenSize, maxOverlapSize);
        this.patterns = patterns;
        this.stopPatterns = stopPatterns;

        // 추출 범위 분리
        int head = 0, tail = 0;
        List<String> prefixes = new ArrayList<>();
        for (PatternVo pattern : patterns)
            prefixes.addAll(pattern.getPrefixes().stream().map(PrefixVo::getPrefix).toList());

        while (tail < extractContents.size()) {
            ExtractContent extractContent = extractContents.get(tail++);
            // 중단 조건 확인
            if (isPatternMatch(extractContent, stopPatterns)) break;
            // 일치 조건 확인
            if (head == 0 && isPatternMatch(extractContent, prefixes)) {
                head = tail - 1;
            }
        }
        this.extractContents = extractContents.subList(head, tail);

        this.flushContent();
    }

    /**
     * 청크 생성자
     *
     * @param parent          부모 청크
     * @param extractContents 추출 본문 목록
     */
    public ExtractChunk(ExtractChunk parent, List<ExtractContent> extractContents) {
        super(parent.depth + 1, parent.depthSize, parent.maxTokenSize, parent.overlapSize, deepCopyTitleBuffers(parent.titleBuffers));
        this.patterns = parent.patterns;
        this.stopPatterns = parent.stopPatterns;
        this.extractContents = extractContents;
    }

    /**
     * 청킹 프로 세스
     *
     * @param chunk 부모 청크
     * @return 문서 청크 목록
     */
    private static List<Chunk> chunking(ExtractChunk chunk) {
        int nextDepth = chunk.depth + 1;
        int tokenSize = chunk.generateContent().length() + chunk.generateSubContent().length();

        if (nextDepth >= chunk.depthSize) {
            // 청크 본문 저장
            chunk.flushContent();

            // 뎁스 초과
            List<Chunk> chunksByToken = new ArrayList<>();

            // TODO: maxTokenSize 기준으로 마지막 청킹
            chunksByToken.add(chunk);

            return tokenSize == 0
                    ? Collections.emptyList()
                    : chunksByToken;
        } else if (tokenSize <= chunk.patterns.get(nextDepth).getTokenSize()) {
            // 청크 본문 저장
            chunk.flushContent();
            // 토큰 수 적합
            return tokenSize == 0
                    ? Collections.emptyList()
                    : List.of(chunk);
        }

        List<Chunk> chunks = new ArrayList<>();
        int head = -1;
        PatternVo patternVo = chunk.patterns.get(nextDepth);
        for (int contentIndex = 0; contentIndex < chunk.extractContents.size(); contentIndex++) {
            ExtractContent extractContent = chunk.extractContents.get(contentIndex);
            for (int prefixIndex = 0; prefixIndex < patternVo.getPrefixes().size(); prefixIndex++) {
                PrefixVo prefix = patternVo.getPrefixes().get(prefixIndex);
                Pattern pattern = Pattern.compile(prefix.getPrefix(), Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(extractContent.getContent());

                if (matcher.find()) {
                    // 정규식 설정
                    extractContent.setPrefix(prefix.getPrefix());

                    // 재귀
                    chunks.addAll(chunking(new ExtractChunk(
                            chunk, chunk.extractContents.subList(Math.max(head, 0), contentIndex))));

                    // 헤드 인덱스 변경
                    head = contentIndex;

                    // 타이틀 버퍼 정리
                    chunk.titleBufferClear(nextDepth, prefixIndex);

                    // 타이틀 지정
                    if (!prefix.getIsDeleting()) {
                        chunk.titleBuffers[nextDepth][prefixIndex] = new ChunkTitle(
                                matcher.group().trim(),
                                matcher.group().trim());
                    }
                    break;
                }
            }
        }

        chunks.addAll(chunking(new ExtractChunk(
                chunk, chunk.extractContents.subList(Math.max(head, 0), chunk.extractContents.size()))));

        return chunks;
    }

    /**
     * 청킹
     */
    @Override
    public List<Chunk> chunking() {
        // 타이틀 버퍼 초기화
        for (int depth = 0; depth < this.depthSize; depth++) {
            int titleBufferSize = patterns.get(depth).getPrefixes().size();
            this.titleBuffers[depth] = new ChunkTitle[titleBufferSize];
            Arrays.fill(this.titleBuffers[depth], null);
        }

        return chunking(this);
    }

    /**
     * 청크 본문 저장
     */
    @Override
    public void flushContent() {
        this.content = this.generateContent();
        this.subContent = this.generateSubContent();
        this.tokenSize = this.content.length() + this.subContent.length();
    }

    /**
     * 본문 생성
     *
     * @return 본문 문자열
     */
    @Override
    public String generateContent() {
        StringBuilder contentBuilder = new StringBuilder();
        if (!this.extractContents.isEmpty()) {
            contentBuilder.append(this.extractContents.getFirst().getSimpleContent());
            for (int contentIndex = 1; contentIndex < this.extractContents.size(); contentIndex++) {
                ExtractContent extractContent = this.extractContents.get(contentIndex);
                String content = extractContent.getContent().trim();
                contentBuilder.append("\n").append(content.trim());
            }
        }
        return contentBuilder.toString().trim();
    }

    /**
     * 부가 본문 생성
     *
     * @return 부가 본문 문자열
     */
    @Override
    public String generateSubContent() {
        return "";
    }

    /**
     * 패턴 확인
     *
     * @param extractContent 문서 라인
     * @param prefixes       패턴 목록
     * @return 패턴 일치 여부
     */
    private static boolean isPatternMatch(ExtractContent extractContent, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (isPatternMatch(extractContent, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 패턴 확인
     *
     * @param extractContent 문서 라인
     * @param prefix         정규식
     * @return 패턴 일치 여부
     */
    private static boolean isPatternMatch(ExtractContent extractContent, String prefix) {
        Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(extractContent.getContent());
        return matcher.find();
    }
}