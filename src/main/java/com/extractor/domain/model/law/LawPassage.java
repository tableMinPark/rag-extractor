package com.extractor.domain.model.law;

import com.extractor.domain.model.Passage;
import com.extractor.domain.vo.pattern.PatternVo;
import com.extractor.domain.vo.pattern.PrefixVo;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 법령 기반 청킹
 */
@ToString
public class LawPassage extends Passage {

    private final List<LawContent> lawContents;

    private final Map<Long, List<LawContent>> lawLinks;

    private final List<PatternVo> patterns;

    private final List<String> excludeContentTypes;

    /**
     * 초기 생성자
     *
     * @param docId               문서 식별자
     * @param lawContents         법령 본문 목록
     * @param patterns            패시지 패턴
     * @param excludeContentTypes 제외 타입 목록
     */
    public LawPassage(String docId, List<LawContent> lawContents, Map<Long, List<LawContent>> lawLinks, List<PatternVo> patterns, List<String> excludeContentTypes) {
        super(docId, -1, patterns.size());
        this.patterns = patterns;
        this.excludeContentTypes = excludeContentTypes;
        this.lawLinks = lawLinks;

        // 타이틀 버퍼 초기화
        for (int depth = 0; depth < this.depthSize; depth++) {
            int titleBufferSize = patterns.get(depth).getPrefixes().size();
            this.titleBuffers[depth] = new String[titleBufferSize];
            Arrays.fill(this.titleBuffers[depth], "");
        }

        // 추출 범위 분리 (제외 타입 필터링)
        this.lawContents = lawContents.stream()
                .filter(lawContent -> !this.excludeContentTypes.contains(lawContent.getCategoryCode()))
                .collect(Collectors.toList());

        // 패시지 본문 저장
        this.setContent(this.lawContents, this.lawLinks);
    }

    /**
     * 청킹 패시지 생성자
     *
     * @param parent      부모 패시지
     * @param lawContents 법령 본문 목록
     */
    public LawPassage(LawPassage parent, List<LawContent> lawContents) {
        super(parent.docId, parent.depth + 1, parent.depthSize, deepCopyTitleBuffers(parent.titleBuffers));
        this.patterns = parent.patterns;
        this.excludeContentTypes = parent.excludeContentTypes;
        this.lawLinks = parent.lawLinks;
        this.lawContents = lawContents;

        // 패시지 본문 저장
        this.setContent(this.lawContents, this.lawLinks);
    }

    /**
     * 패시지 셀렉트 (호출)
     */
    @Override
    public List<Passage> chunk() {
        return chunk(this);
    }

    /**
     * 패시지 셀렉트 (재귀)
     *
     * @param passage 패시지 문서
     * @return 청킹 패시지 문서 목록
     */
    public List<Passage> chunk(LawPassage passage) {
        int nextDepth = passage.depth + 1;
        int tokenSize = passage.getTokenSize();

        // 뎁스 초과 or 토큰 수 적합
        if (nextDepth >= passage.depthSize || tokenSize <= patterns.get(nextDepth).getTokenSize()) {
            return tokenSize == 0
                    ? Collections.emptyList()
                    : List.of(passage);
        }

        List<Passage> passages = new ArrayList<>();

        int head = -1;
        PatternVo patternVo = passage.patterns.get(nextDepth);
        for (int contentIndex = 0; contentIndex < passage.lawContents.size(); contentIndex++) {
            LawContent lawContent = passage.lawContents.get(contentIndex);
            for (int prefixIndex = 0; prefixIndex < patternVo.getPrefixes().size(); prefixIndex++) {
                PrefixVo prefix = patternVo.getPrefixes().get(prefixIndex);

                if (prefix.getPrefix().equals(lawContent.getCategoryCode())) {
                    // 재귀
                    passages.addAll(chunk(new LawPassage(
                            passage, passage.lawContents.subList(Math.max(head, 0), contentIndex))));

                    // 헤드 인덱스 변경
                    head = contentIndex;

                    // 타이틀 버퍼 정리
                    passage.titleBufferClear(nextDepth, prefixIndex);

                    // 타이틀 지정
                    if (!prefix.getIsDeleting()) {
                        passage.titleBuffers[nextDepth][prefixIndex] = lawContent.getTitle();
                    }
                    break;
                }
            }
        }

        passages.addAll(chunk(new LawPassage(
                passage, passage.lawContents.subList(Math.max(head, 0), passage.lawContents.size()))));

        return passages;
    }

    /**
     * 패시지 본문 저장
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
