package com.genai.embed.repository.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
public class DeleteIndexBulkRequest {

    private final Delete delete;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delete {

        @JsonProperty("_id")
        private String id;
    }
}
