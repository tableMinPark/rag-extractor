package com.genai.embed.repository.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetCollectionResponse {

    private Mappings mappings;
    private Settings settings;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mappings {

        @JsonProperty("properties")
        private Map<String, Map<String, Object>> properties;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Settings {

        private Index index;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Index {

            @JsonProperty("number_of_shards")
            private String numberOfShards;

            @JsonProperty("number_of_replicas")
            private String numberOfReplicas;
        }
    }
}
