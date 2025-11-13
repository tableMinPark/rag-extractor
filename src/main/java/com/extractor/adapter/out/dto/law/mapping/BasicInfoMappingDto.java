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
public class BasicInfoMappingDto {

    @JsonAlias("법령ID")
    private String lawId;

    @JsonAlias("법령명_한글")
    private String name;

    @JsonAlias("소관부처")
    private Department department;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Department {

        @JsonAlias("content")
        private String name;

        @JsonAlias("소관부처코드")
        private String code;
    }
}
