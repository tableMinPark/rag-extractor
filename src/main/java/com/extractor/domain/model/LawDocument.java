package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@Getter
public class LawDocument {

    private final Long lawId;

    private final String lawName;

    private final List<LawContent> lawContents;

    private final Map<Long, List<LawLink>> lawLinks;

    @Builder
    public LawDocument(Long lawId, String lawName, List<LawContent> lawContents, Map<Long, List<LawLink>> lawLinks) {
        this.lawId = lawId;
        this.lawName = lawName;
        this.lawContents = lawContents;
        this.lawLinks = lawLinks;
    }

    /**
     * 본문 전체 조회
     * @return 본문 전체 문자열
     */
    public String getContent() {
        StringBuilder contentBuilder = new StringBuilder();

        this.lawContents.forEach(lawContent -> {
            contentBuilder
                    .append("\n")
                    .append(lawContent.getTitle())
                    .append("\n")
                    .append(lawContent.getContent());
        });

        return contentBuilder.toString().trim();
    }
}