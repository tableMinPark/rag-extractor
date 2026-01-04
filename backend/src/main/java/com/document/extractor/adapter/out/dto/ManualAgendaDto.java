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
public class ManualAgendaDto {

    private String uuid;

    private String id;

    private String idTitle;

    private String title;

    @JsonAlias("trns")
    private String tractionId;

    private String lastUpt;

    private String lastTime;

    @JsonAlias("sub")
    private List<ManualAgendaDto> contents = new ArrayList<>();

    public ManualAgendaDto(String uuid, String title, String tractionId) {
        this.uuid = uuid;
        this.title = title;
        this.tractionId = tractionId;
    }

    public static List<ManualAgendaDto> toList(ManualAgendaDto now) {
        List<ManualAgendaDto> manualAgendaDtoList = new ArrayList<>();

        manualAgendaDtoList.add(now);

        for (ManualAgendaDto next : now.getContents()) {
            manualAgendaDtoList.addAll(toList(next));
        }

        return manualAgendaDtoList;
    }
}
