package com.extractor.adapter.out.dto.manual;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualAgendaDto {

    private String uuid;

    private String id;

    private String idTitle;

    private String title;

    @JsonAlias("trns")
    private String tractionId;

    private String lastUpt;

    private String lastTime;
}
