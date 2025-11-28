package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.application.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {
        ExtractController.class,
        ChunkController.class,
        ChunkBatchController.class,
})
public class ControllerAdvice {

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundDocumentException(NotFoundException e) {

        log.error(e.getMessage());

        return ResponseEntity.internalServerError().body(ErrorResponseDto.builder()
                .message(e.getMessage())
                .stackTrace(e.getStackTrace())
                .build());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException e) {

        log.error(e.getMessage());

        return ResponseEntity.internalServerError().body(ErrorResponseDto.builder()
                .message(e.getMessage())
                .stackTrace(e.getStackTrace())
                .build());
    }
}
