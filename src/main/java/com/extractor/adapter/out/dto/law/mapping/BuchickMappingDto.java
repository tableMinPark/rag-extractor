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
public class BuchickMappingDto {

    @JsonAlias("부칙단위")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<BuchickMappingData> data;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BuchickMappingData {

        @JsonAlias("부칙키")
        private String key;

        @JsonAlias("부칙공포번호")
        private String promulgationNum;

        @JsonAlias("부칙공포일자")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
        private LocalDate promulgationDate;

        @JsonAlias("부칙내용")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> contents;

        /**
         * 내용 setter (2차원 리스트 병합)
         */
        public void setContents(List<List<String>> contents) {
            List<String> mergeContent = new ArrayList<>();

            for (List<String> content : contents) {
                mergeContent.addAll(content.stream().map(String::trim).toList());
            }

            this.contents = mergeContent;
        }
    }
}
