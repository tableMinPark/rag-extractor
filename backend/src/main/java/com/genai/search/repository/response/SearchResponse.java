package com.genai.search.repository.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.wrapper.Search;
import lombok.*;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T extends DocumentEntity> {

    private Integer took;

    @JsonProperty("timed_out")
    private Boolean timeout;

    @JsonProperty("hits")
    private Result<T> result;

    public record Result<T extends DocumentEntity>(
        Total total,
        @JsonProperty("max_score") float maxScore,
        List<Search<T>> hits
    ) {
        public record Total(int value, String relation) {}
    }
}
