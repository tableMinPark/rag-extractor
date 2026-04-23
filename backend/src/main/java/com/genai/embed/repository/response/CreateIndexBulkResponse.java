package com.genai.embed.repository.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateIndexBulkResponse {

    private Integer took;
    private Boolean errors;
}
