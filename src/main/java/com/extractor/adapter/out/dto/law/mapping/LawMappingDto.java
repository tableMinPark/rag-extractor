package com.extractor.adapter.out.dto.law.mapping;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LawMappingDto {

    @JsonAlias("법령ID")
    private String lawId;

    @JsonAlias("id")
    private Long num;

    @JsonAlias("법령일련번호")
    private Long lawNum;

    @JsonAlias("법령상세링크")
    private String url;

    @JsonAlias("법령명한글")
    private String name;

    @JsonAlias("법령약칭명")
    private String shortName;

    @JsonAlias("법령구분명")
    private String categoryName;

    @JsonAlias("소관부처코드")
    private String departmentCode;

    @JsonAlias("소관부처명")
    private String departmentName;

    @JsonAlias("공포번호")
    private Integer promulgationNum;

    @JsonAlias("현행연혁코드")
    private String activeCode;

    @JsonAlias("시행일자")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate activeDate;

    @JsonAlias("공포일자")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate promulgationDate;
}
