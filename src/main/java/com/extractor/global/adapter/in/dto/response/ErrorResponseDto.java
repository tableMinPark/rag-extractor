package com.extractor.global.adapter.in.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    @Schema(description = "에러 메시지")
    private String message;

    @Schema(description = "예외 스택 목록")
    private StackTraceElement[] stackTrace;
}
