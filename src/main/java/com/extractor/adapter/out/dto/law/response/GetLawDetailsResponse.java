package com.extractor.adapter.out.dto.law.response;

import com.extractor.adapter.out.dto.law.mapping.LawDetailMappingDto;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetLawDetailsResponse {

    @JsonAlias("법령")
    private LawDetailMappingDto result;
}
