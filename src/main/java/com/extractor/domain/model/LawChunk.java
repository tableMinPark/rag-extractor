package com.extractor.domain.model;

import com.extractor.domain.processor.ChunkProcessor;
import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 법령 기반 청킹
 */
@Slf4j
@ToString
public class LawChunk extends Chunk implements ChunkProcessor {

    private final List<LawContent> lawContents;

    private final List<PatternVo> patterns;

    private final List<String> excludeContentTypes;

    /**
     * 루트 청크 생성자
     *
     * @param lawContents         법령 본문 목록
     * @param patterns            청크 패턴
     * @param excludeContentTypes 제외 타입 목록
     */
    public LawChunk(List<LawContent> lawContents, List<PatternVo> patterns, List<String> excludeContentTypes, int maxTokenSize, int overlapSize) {
        super(-1, patterns.size(), maxTokenSize, overlapSize);
        this.patterns = patterns;
        this.excludeContentTypes = excludeContentTypes;

        // 추출 범위 분리 (제외 타입 필터링)
        this.lawContents = lawContents.stream()
                .filter(lawContent -> !this.excludeContentTypes.contains(lawContent.getCategoryCode()))
                .collect(Collectors.toList());

        // 루트 청크 플러시
        this.flushContent();
    }

    /**
     * 청크 생성자
     *
     * @param parent      부모 청크
     * @param lawContents 법령 본문 목록
     */
    public LawChunk(LawChunk parent, List<LawContent> lawContents) {
        super(parent.depth + 1, parent.depthSize, parent.maxTokenSize, parent.overlapSize, deepCopyTitleBuffers(parent.titleBuffers));
        this.patterns = parent.patterns;
        this.excludeContentTypes = parent.excludeContentTypes;
        this.lawContents = lawContents;
    }

    /**
     * 청킹 프로 세스
     *
     * @param chunk 법령 청크
     * @return 법령 청크 목록
     */
    private static List<Chunk> chunk(LawChunk chunk) {
        int nextDepth = chunk.depth + 1;
        int tokenSize = chunk.generateContent().length() + chunk.generateSubContent().length();

        // 뎁스 초과
        if (nextDepth >= chunk.depthSize) {
            List<Chunk> chunksByToken = new ArrayList<>();

            if (tokenSize > chunk.maxTokenSize) {
                // 토큰 수 초과 & content 하나인 경우
                if (chunk.lawContents.size() == 1) {
                    Queue<ChunkTokenContent> contentQueue = new ArrayDeque<>();
                    // 문자 기준 분리
                    for (int depth = 0; depth < TOKEN_CHUNKING_PREFIXES.length; depth++) {
                        String prefix = TOKEN_CHUNKING_PREFIXES[depth];
                        for (String splitContent : chunk.generateContent().trim().split(prefix)) {
                            contentQueue.offer(ChunkTokenContent.builder()
                                    .depth(depth + 1)
                                    .content(splitContent)
                                    .build());
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
                        String subContent = chunk.generateSubContent(content, chunk.lawContents.getFirst().getLawLinks());
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
                                chunksByToken.add(new Chunk(
                                        chunk.depth,
                                        chunk.depthSize,
                                        chunk.maxTokenSize,
                                        chunk.overlapSize,
                                        Chunk.deepCopyTitleBuffers(chunk.titleBuffers),
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

                    // 임시 저장 문자열 플러시
                    if (!contentBuilder.toString().trim().isBlank() && !subContentBuilder.toString().trim().isBlank()) {
                        chunksByToken.add(new Chunk(
                                chunk.depth,
                                chunk.depthSize,
                                chunk.maxTokenSize,
                                chunk.overlapSize,
                                Chunk.deepCopyTitleBuffers(chunk.titleBuffers),
                                contentBuilder.toString(),
                                subContentBuilder.toString()));
                    }

                    if (chunksByToken.isEmpty() && tokenSize > 0) {
                        chunk.flushContent();
                        chunksByToken.add(chunk);
                    }

                } else if (chunk.lawContents.size() > 1) {
                    int mid = chunk.lawContents.size() / 2;
                    chunksByToken.addAll(chunk(new LawChunk(chunk, chunk.lawContents.subList(0, mid))));
                    chunksByToken.addAll(chunk(new LawChunk(chunk, chunk.lawContents.subList(mid, chunk.lawContents.size()))));
                }

                // 오버랩 적용
                setOverlap(chunk.overlapSize, chunksByToken);

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

        List<Chunk> chunks = new ArrayList<>();

        int head = -1;
        PatternVo patternVo = chunk.patterns.get(nextDepth);
        for (int contentIndex = 0; contentIndex < chunk.lawContents.size(); contentIndex++) {
            LawContent lawContent = chunk.lawContents.get(contentIndex);
            for (int prefixIndex = 0; prefixIndex < patternVo.getPrefixes().size(); prefixIndex++) {
                PrefixVo prefix = patternVo.getPrefixes().get(prefixIndex);

                if (prefix.getPrefix().equals(lawContent.getCategoryCode())) {
                    // 재귀
                    chunks.addAll(chunk(new LawChunk(chunk, chunk.lawContents.subList(Math.max(head, 0), contentIndex))));

                    // 헤드 인덱스 변경
                    head = contentIndex;

                    // 타이틀 버퍼 정리
                    chunk.titleBufferClear(nextDepth, prefixIndex);

                    // 타이틀 지정
                    if (!prefix.getIsDeleting()) {
                        String title = lawContent.getTitle();
                        String simpleTitle = !lawContent.getSimpleTitle().trim().isBlank()
                                ? lawContent.getSimpleTitle()
                                : lawContent.getTitle();

                        chunk.titleBuffers[nextDepth][prefixIndex] = new ChunkTitle(title, simpleTitle);
                    }
                    break;
                }
            }
        }

        chunks.addAll(chunk(new LawChunk(chunk, chunk.lawContents.subList(Math.max(head, 0), chunk.lawContents.size()))));

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

        return chunk(this);
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
        // 본문 저장
        StringBuilder contentBuilder = new StringBuilder();
        if (!this.lawContents.isEmpty()) {
            contentBuilder.append(this.lawContents.getFirst().getContent());
            for (int contentIndex = 1; contentIndex < this.lawContents.size(); contentIndex++) {
                LawContent lawContent = this.lawContents.get(contentIndex);
                contentBuilder.append("\n")
                        .append(lawContent.getTitle().trim())
                        .append(" ")
                        .append(lawContent.getContent().trim());
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
        // 연결 데이터 저장
        StringBuilder subContentBuilder = new StringBuilder();
        lawContents.forEach(lawContent -> lawContent.getLawLinks().stream()
                .filter(lawLink -> lawContent.getContent().contains(lawLink.getText()))
                .forEach(lawLink -> subContentBuilder.append("\n")
                        .append(lawLink.getTitle().trim())
                        .append(" ")
                        .append(lawLink.getContent().trim())));
        return subContentBuilder.toString().trim();
    }

    /**
     * 부가 본문 생성
     *
     * @param content  본문
     * @param lawLinks 연결 본문
     * @return 부가 본문 문자열
     */
    private String generateSubContent(String content, List<LawLink> lawLinks) {
        StringBuilder subContentBuilder = new StringBuilder();
        lawLinks.stream()
                .filter(lawLink -> content.contains(lawLink.getText()))
                .forEach(lawLink -> subContentBuilder.append("\n")
                        .append(lawLink.getTitle().trim())
                        .append(" ")
                        .append(lawLink.getContent().trim()));
        return subContentBuilder.toString().trim();
    }

    /**
     * 청킹 데이터 오버랩 적용
     *
     * @param overlapSize 오버랩 사이즈
     * @param chunks      청킹 데이터 목록
     */
    private static void setOverlap(int overlapSize, List<Chunk> chunks) {

        List<String> contents = chunks.stream().map(Chunk::getContent).toList();

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