package com.genai.search.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SearchRequestDto {

    @NotBlank
    private String query;

    @NotBlank
    private String collectionId;

    @NotNull
    @Positive
    private Integer topK;

    private List<String> aliases = List.of();
}
