package com.document.extractor.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class ManualTableContentDto {

    private String uuid;

    private String title;

    @JsonAlias("sub")
    private List<ManualAgendaDto> contents = new ArrayList<>();

    @JsonAlias("trns")
    private String tractionId;
}
