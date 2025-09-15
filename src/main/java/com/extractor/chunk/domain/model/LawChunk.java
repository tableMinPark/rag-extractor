package com.extractor.chunk.domain.model;

import com.extractor.chunk.domain.vo.PatternVo;
import com.extractor.chunk.domain.vo.PrefixVo;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 법령 기반 청킹
 */
@Slf4j
@ToString
public class LawChunk extends Chunk {

    private final List<LawContent> lawContents;

    private final Map<Long, List<LawContent>> lawLinks;

    private final List<PatternVo> patterns;

    private final List<String> excludeContentTypes;

    /**
     * 루트 청크 생성자
     *
     * @param lawContents         법령 본문 목록
     * @param patterns            청크 패턴
     * @param excludeContentTypes 제외 타입 목록
     */
    public LawChunk(List<LawContent> lawContents, Map<Long, List<LawContent>> lawLinks, List<PatternVo> patterns, List<String> excludeContentTypes) {
        super(-1, patterns.size());
        this.patterns = patterns;
        this.excludeContentTypes = excludeContentTypes;
        this.lawLinks = lawLinks;

        // 추출 범위 분리 (제외 타입 필터링)
        this.lawContents = lawContents.stream()
                .filter(lawContent -> !this.excludeContentTypes.contains(lawContent.getCategoryCode()))
                .collect(Collectors.toList());

        // 청크 본문 저장
        this.setContent(this.lawContents, this.lawLinks);
    }

    /**
     * 청크 생성자
     *
     * @param parent      부모 청크
     * @param lawContents 법령 본문 목록
     */
    public LawChunk(LawChunk parent, List<LawContent> lawContents) {
        super(parent.depth + 1, parent.depthSize, deepCopyTitleBuffers(parent.titleBuffers));
        this.patterns = parent.patterns;
        this.excludeContentTypes = parent.excludeContentTypes;
        this.lawLinks = parent.lawLinks;
        this.lawContents = lawContents;

        // 청크 본문 저장
        this.setContent(this.lawContents, this.lawLinks);
    }

    /**
     * 청킹 (호출)
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
     * 청킹 (재귀)
     *
     * @param chunk 법령 청크
     * @return 법령 청크 목록
     */
    public List<Chunk> chunk(LawChunk chunk) {
        int nextDepth = chunk.depth + 1;
        int tokenSize = chunk.getTokenSize();

        // 뎁스 초과 or 토큰 수 적합
        if (nextDepth >= chunk.depthSize || tokenSize <= chunk.patterns.get(nextDepth).getTokenSize()) {
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
                    chunks.addAll(chunk(new LawChunk(
                            chunk, chunk.lawContents.subList(Math.max(head, 0), contentIndex))));

                    // 헤드 인덱스 변경
                    head = contentIndex;

                    // 타이틀 버퍼 정리
                    chunk.titleBufferClear(nextDepth, prefixIndex);

                    // 타이틀 지정
                    if (!prefix.getIsDeleting()) {
                        chunk.titleBuffers[nextDepth][prefixIndex] = new ChunkTitle(
                                lawContent.getTitle(),
                                lawContent.getSimpleTitle().isBlank()
                                        ? lawContent.getTitle()
                                        : lawContent.getSimpleTitle());
                    }
                    break;
                }
            }
        }

        chunks.addAll(chunk(new LawChunk(
                chunk, chunk.lawContents.subList(Math.max(head, 0), chunk.lawContents.size()))));

        return chunks;
    }

    /**
     * 청크 본문 저장
     *
     * @param lawContents 법령 본문 목록
     */
    private void setContent(List<LawContent> lawContents, Map<Long, List<LawContent>> lawLinks) {
        // 본문 저장
        if (!lawContents.isEmpty()) {
            StringBuilder contentBuilder = new StringBuilder(lawContents.getFirst().getContent());
            for (int contentIndex = 1; contentIndex < lawContents.size(); contentIndex++) {
                LawContent lawContent = lawContents.get(contentIndex);
                contentBuilder.append("\n")
                        .append(lawContent.getTitle().trim())
                        .append(" ")
                        .append(lawContent.getContent().trim());
            }
            this.content = contentBuilder.toString().trim();
        }

        // 연결 데이터 저장
        StringBuilder subContentBuilder = new StringBuilder();
        lawContents.stream().mapToLong(LawContent::getLawContentId).forEach(lawContentId -> {
            if (lawLinks.containsKey(lawContentId)) {
                lawLinks.get(lawContentId).forEach(lawContent -> {
                    subContentBuilder.append("\n")
                            .append(lawContent.getTitle().trim())
                            .append(" ")
                            .append(lawContent.getContent().trim());
                });
            }
        });
        this.subContent = subContentBuilder.toString().trim();
    }
}