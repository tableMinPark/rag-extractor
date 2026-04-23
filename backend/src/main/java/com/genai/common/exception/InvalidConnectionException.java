package com.genai.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvalidConnectionException extends RuntimeException {

    private final String target;
}
