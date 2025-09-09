package com.extractor.domain.model.law;

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

    private final Map<Long, List<LawContent>> lawLinks;

    @Builder
    public LawDocument(Long lawId, String lawName, List<LawContent> lawContents, Map<Long, List<LawContent>> lawLinks) {
        this.lawId = lawId;
        this.lawName = lawName;
        this.lawContents = lawContents;
        this.lawLinks = lawLinks;
    }
}