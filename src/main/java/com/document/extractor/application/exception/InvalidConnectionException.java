package com.document.extractor.application.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvalidConnectionException extends RuntimeException {

    private final String target;
}
