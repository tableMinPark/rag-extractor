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
public class HangMappingDto {

    @JsonAlias("항번호")
    private String num;

    @JsonAlias("항내용")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> contents;

    @JsonAlias("호")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<HoMappingDto> hoMappingDtos = new ArrayList<>();

    public void setContents(List<List<String>> contents) {
        List<String> mergeContents = new ArrayList<>();

        for (List<String> content : contents) {
            mergeContents.addAll(content.stream().map(String::trim).toList());
        }

        this.contents = mergeContents;
    }
}
