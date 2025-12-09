package com.document.extractor.adapter.in.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseDto<T> {

    @Schema(description = "코드")
    private int code;

    @Schema(description = "메시지")
    private String message;
    private String status;
    private T result;

    @Builder
    public ResponseDto(int code, String message, String status, T result) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.result = result;
    }
}
