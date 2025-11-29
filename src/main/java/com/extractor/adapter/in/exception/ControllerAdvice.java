package com.extractor.adapter.in.exception;

import com.extractor.adapter.in.ChunkController;
import com.extractor.adapter.in.ExtractController;
import com.extractor.adapter.in.SourceController;
import com.extractor.adapter.in.response.ResponseDto;
import com.extractor.application.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice(basePackageClasses = {
        ExtractController.class,
        ChunkController.class,
        SourceController.class,
})
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(ResponseDto.builder()
                .message(e.getBindingResult()
                        .getFieldErrors()
                        .getFirst()
                        .getDefaultMessage())
                .data(Collections.emptyMap())
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(ResponseDto.builder()
                .message(e.getMessage())
                .data(Collections.emptyMap())
                .build());
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleNotFoundDocumentException(NotFoundException e) {
        return ResponseEntity.internalServerError().body(ResponseDto.builder()
                .message(e.getMessage())
                .data(Collections.emptyMap())
                .build());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ResponseDto<?>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(ResponseDto.builder()
                .message(e.getMessage())
                .data(Collections.emptyMap())
                .build());
    }
}
