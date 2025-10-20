package com.extractor.domain.model;

import com.extractor.domain.processor.ChunkProcessor;
import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
import lombok.ToString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 한줄 단위 패턴 & 토큰 수 기반 청킹
 */
@ToString
public class ExtractChunk extends ChunkOld implements ChunkProcessor {

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
    private static List<ChunkOld> chunking(ExtractChunk chunk) {
        int nextDepth = chunk.depth + 1;
        int tokenSize = chunk.generateContent().length() + chunk.generateSubContent().length();

        if (nextDepth >= chunk.depthSize) {
            // 청크 본문 저장
            chunk.flushContent();

            // 뎁스 초과
            List<ChunkOld> chunksByToken = new ArrayList<>();

            if (tokenSize > chunk.maxTokenSize) {
                // content 가 하나 이상인 경우
                if (chunk.extractContents.size() > 1) {
                    // 글자 수 기준 청킹
                    int mid = chunk.extractContents.size() / 2;
                    chunksByToken.addAll(chunking(new ExtractChunk(chunk, chunk.extractContents.subList(0, mid))));
                    chunksByToken.addAll(chunking(new ExtractChunk(chunk, chunk.extractContents.subList(mid, chunk.extractContents.size()))));
                }
                // content 하나인 경우
                else if (chunk.extractContents.size() == 1) {
                    Queue<ChunkOld> chunkQueue = new ArrayDeque<>();
                    Queue<ChunkTokenContent> contentQueue = new ArrayDeque<>();

                    // 문자 기준 분리
                    for (int depth = 0; depth < TOKEN_CHUNKING_PREFIXES.length; depth++) {
                        String prefix = TOKEN_CHUNKING_PREFIXES[depth];
                        for (String splitContent : chunk.generateContent().trim().split(prefix)) {
                            if (!splitContent.trim().isBlank()) {
                                contentQueue.offer(ChunkTokenContent.builder()
                                        .depth(depth + 1)
                                        .content(splitContent)
                                        .build());
                            }
                        }
                        if (!contentQueue.isEmpty()) break;
                    }

                    // 임시 저장
                    StringBuilder contentBuilder = new StringBuilder();
                    StringBuilder subContentBuilder = new StringBuilder();

                    // 분리 문자열 Queue
                    while (!contentQueue.isEmpty()) {
                        ChunkTokenContent chunkTokenContent = contentQueue.poll();

                        int depth = chunkTokenContent.getDepth();
                        String content = chunkTokenContent.getContent();
                        String subContent = chunk.generateSubContent();
                        int splitContentTokenSize = content.length() + subContent.length();

                        if (splitContentTokenSize > chunk.maxTokenSize && depth < TOKEN_CHUNKING_PREFIXES.length) {
                            String prefix = TOKEN_CHUNKING_PREFIXES[depth];
                            // 재분리
                            for (String splitContent : chunk.generateContent().trim().split(prefix)) {
                                contentQueue.offer(ChunkTokenContent.builder()
                                        .depth(depth + 1)
                                        .content(splitContent)
                                        .build());
                            }
                        } else if (splitContentTokenSize > 0) {
                            // 더이상 분리 불가능 or 분리 문자열 토큰 제약 충족
                            int splitTokenSize = contentBuilder.length() + subContentBuilder.length() + splitContentTokenSize;

                            // 임시 저장 문자열 플러시
                            if (splitTokenSize > chunk.maxTokenSize) {
                                chunksByToken.add(new ChunkOld(
                                        chunk.depth,
                                        chunk.depthSize,
                                        chunk.maxTokenSize,
                                        chunk.overlapSize,
                                        ChunkOld.deepCopyTitleBuffers(chunk.titleBuffers),
                                        contentBuilder.toString(),
                                        subContentBuilder.toString()));

                                contentBuilder = new StringBuilder();
                                subContentBuilder = new StringBuilder();
                            }

                            // 문자열 임시 저장
                            contentBuilder.append(content);
                            subContentBuilder.append(subContent);
                        }
                    }
                }
                // 오버랩 적용
                // setOverlap(chunk.overlapSize, chunksByToken);
            } else if (tokenSize > 0) {
                //  0 < 토큰 수 < maxTokenSize 인 경우
                chunk.flushContent();
                chunksByToken.add(chunk);
            }

            return chunksByToken;

        }
        // 아래 depth 가 있지만, 토큰 수 제한을 충족한 경우
        else if (tokenSize <= chunk.patterns.get(nextDepth).getTokenSize()) {
            // 청크 본문 저장
            chunk.flushContent();
            // 토큰 수 적합
            return tokenSize == 0
                    ? Collections.emptyList()
                    : List.of(chunk);
        }

        List<ChunkOld> chunks = new ArrayList<>();

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

        chunks.addAll(chunking(new ExtractChunk(chunk, chunk.extractContents.subList(Math.max(head, 0), chunk.extractContents.size()))));

        return chunks;
    }

    /**
     * 청킹
     */
    @Override
    public List<ChunkOld> chunking() {
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