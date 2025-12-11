package com.document.extractor.adapter.in.exception;

import com.document.extractor.adapter.in.ChunkBatchController;
import com.document.extractor.adapter.in.ChunkController;
import com.document.extractor.adapter.in.ExtractController;
import com.document.extractor.adapter.in.SourceController;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.in.enums.Response;
import com.document.extractor.application.exception.InvalidConnectionException;
import com.document.extractor.application.exception.InvalidSourceTypeException;
import com.document.extractor.application.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice(basePackageClasses = {
        SourceController.class,
        ExtractController.class,
        ChunkController.class,
        ChunkBatchController.class,
})
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        Set<String> invalidFields = new LinkedHashSet<>();

        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("'");
            builder.append(fieldError.getField());
            builder.append("'(은)는 ");
            builder.append(fieldError.getDefaultMessage());
            builder.append(". ");
            invalidFields.add(fieldError.getField());
        }

        return ResponseEntity
                .status(Response.INVALID_METHOD_PARAMETER.getStatusCode())
                .body(Response.INVALID_METHOD_PARAMETER.toResponseDto(builder.toString(), InValidFieldResultDto.builder()
                                .invalidFields(invalidFields)
                        .build()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<String> invalidFields = new LinkedHashSet<>();

        StringBuilder builder = new StringBuilder();
        e.getConstraintViolations().forEach(violation -> {
            builder.append("'");
            String[] pathParts = violation.getPropertyPath().toString().split("\\.");
            String methodName = pathParts[pathParts.length - 1];
            builder.append(methodName);
            builder.append("'(은)는 ");
            builder.append(violation.getMessage());
            builder.append(". ");
            invalidFields.add(methodName);
        });

        return ResponseEntity
                .status(Response.INVALID_REQUEST_BODY.getStatusCode())
                .body(Response.INVALID_REQUEST_BODY.toResponseDto(builder.toString(), InValidFieldResultDto.builder()
                        .invalidFields(invalidFields)
                        .build()));
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(Response.NOT_FOUND.getStatusCode())
                .body(Response.NOT_FOUND.toResponseDto(Map.of("target", e.getTarget())));
    }

    @ExceptionHandler(value = InvalidSourceTypeException.class)
    public ResponseEntity<ResponseDto<?>> handleInvalidSourceException(InvalidSourceTypeException e) {
        return ResponseEntity.status(Response.INVALID_SOURCE_TYPE.getStatusCode())
                .body(Response.INVALID_SOURCE_TYPE.toResponseDto());
    }

    @ExceptionHandler(value = InvalidConnectionException.class)
    public ResponseEntity<ResponseDto<?>> handleInvalidConnectionException(InvalidConnectionException e) {
        return ResponseEntity.status(Response.INVALID_CONNECTION.getStatusCode())
                .body(Response.INVALID_CONNECTION.toResponseDto(Map.of("target", e.getTarget())));
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ResponseDto<?>> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity.status(Response.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(Response.INTERNAL_SERVER_ERROR.toResponseDto(e.getMessage()));
    }
}
