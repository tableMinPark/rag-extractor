package com.extractor.adapter.out.dto.law.mapping;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoMunMappingDto {

    @JsonAlias("조문단위")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<JoMunMappingData> data;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JoMunMappingData {

        @JsonAlias("조문키")
        private String key;

        @JsonAlias("조문번호")
        private String num;

        @JsonAlias("조문제목")
        private String title;

        @JsonAlias("조문내용")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> contents;

        @JsonAlias("조문시행일자")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
        private LocalDate activeDate;

        @JsonAlias("조문여부")
        private String type;

        @JsonAlias("조문가지번호")
        private String secondsKey;

        @JsonAlias("항")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<HangMappingDto> hangMappingDtos = new ArrayList<>();

        public void setTitle(String title) {
            this.title = title.trim();
        }

        public void setContents(List<List<String>> contents) {
            List<String> mergeContents = new ArrayList<>();

            for (List<String> content : contents) {
                mergeContents.addAll(content.stream().map(String::trim).toList());
            }

            this.contents = mergeContents;
        }
    }
}
