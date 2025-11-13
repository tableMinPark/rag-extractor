package com.extractor.adapter.out.dto.law.mapping;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LawDetailMappingDto {

    @JsonAlias("법령키")
    private String key;

    @JsonAlias("기본정보")
    private BasicInfoMappingDto basicInfoMappingDto;

    @JsonAlias("조문")
    private JoMunMappingDto joMunMappingDto;

    @JsonAlias("부칙")
    private BuchickMappingDto buchickMappingDto;

    @JsonAlias("별표")
    private ByeolpyoMappingDto byeolpyoMappingDto;
}
