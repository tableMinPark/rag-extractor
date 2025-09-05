package com.extractor.adapter.in.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String message;

    private StackTraceElement[] stackTrace;
}
