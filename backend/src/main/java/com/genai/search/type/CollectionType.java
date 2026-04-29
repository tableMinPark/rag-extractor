package com.genai.search.type;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CollectionType {

    private final String collectionId;
    private final String collectionName;
    private final List<String> keywordSearchFields;
    private final List<String> vectorSearchFields;

    @Builder
    private CollectionType(String collectionId, String collectionName,
                           List<String> keywordSearchFields, List<String> vectorSearchFields) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.keywordSearchFields = keywordSearchFields;
        this.vectorSearchFields = vectorSearchFields;
    }

    public static CollectionType ai() {
        return CollectionType.builder()
                .collectionId("gen_ai")
                .collectionName("AI")
                .keywordSearchFields(List.of("title", "sub_title", "third_title", "content"))
                .vectorSearchFields(List.of("vector-context"))
                .build();
    }

    public static CollectionType myai() {
        return CollectionType.builder()
                .collectionId("gen_myai")
                .collectionName("나만의 AI")
                .keywordSearchFields(List.of("title", "sub_title", "third_title", "content"))
                .vectorSearchFields(List.of("vector-context"))
                .build();
    }
}
