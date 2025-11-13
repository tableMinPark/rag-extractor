package com.extractor.adapter.out.dto.law.mapping;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ByeolpyoMappingDto {

    @JsonAlias("별표단위")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ByeolpyoMappingData> data;


    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ByeolpyoMappingData {

        @JsonAlias("별표키")
        private String key;

        @JsonAlias("별표구분")
        private String type;

        @JsonAlias("별표제목")
        private String title;

        @JsonAlias("별표HWP파일명")
        private String hwpFileName;

        @JsonAlias("별표서식파일링크")
        private String hwpFileUrl;

        @JsonAlias("별표PDF파일명")
        private String pdfFileName;

        @JsonAlias("별표서식PDF파일링크")
        private String pdfFileUrl;

        @JsonAlias("별표내용")
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
