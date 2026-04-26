package com.genai.search.repository.response;

import lombok.*;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RerankResponse {
    private List<Document> documents;
    public record Document(String id, String content, float score) {}
}
