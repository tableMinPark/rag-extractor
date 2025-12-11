package com.document.extractor.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractDocument {

    private String status;

    private String message;

    private Result result;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {

        @JsonAlias("total_count")
        @JsonProperty("total_count")
        private Integer totalCount;

        private List<Row> rows;

        private Map<String, Object> location;

        @JsonAlias("copyof")
        @JsonProperty("copyof")
        private int copyOf;

        @JsonAlias("sortkey")
        @JsonProperty("sortkey")
        private List<String> sortKey;
    }

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private Map<String, String> fields;
    }
}
