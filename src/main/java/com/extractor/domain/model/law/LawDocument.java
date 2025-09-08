package com.extractor.domain.model.law;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class LawDocument {

    private final Long lawId;

    private final String lawName;

    private final List<LawContent> lawContents;

    @Builder
    public LawDocument(Long lawId, String lawName, List<LawContent> lawContents) {
        this.lawId = lawId;
        this.lawName = lawName;
        this.lawContents = lawContents;
    }
}
