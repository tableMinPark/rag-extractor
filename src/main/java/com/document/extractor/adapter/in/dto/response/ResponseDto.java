package com.document.extractor.adapter.in.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {

    @Schema(description = "메시지")
    private String message;

    private T data;
}
