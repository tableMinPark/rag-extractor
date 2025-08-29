package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.ExtractHwpxRequestDto;
import com.extractor.adapter.in.dto.ExtractHwpxResponseDto;
import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.usecase.ChunkHwpxUseCase;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "ExtractController")
@RequiredArgsConstructor
@RequestMapping("/extract")
@RestController
public class ExtractController {

    private final ChunkHwpxUseCase chunkHwpxUseCase;

    @Value("${env.upload-path}")
    private String UPLOAD_PATH;

    @PostMapping(path = "/hwp", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ExtractHwpxResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = Map.class))}),
    })
    @Operation(summary = "한글 문서 전처리")
    public ResponseEntity<ExtractHwpxResponseDto> extractHwpx(
            @Parameter(name = "extractHwpxRequestDto", description = "전처리 요청 정보", required = true)
            @RequestPart("requestDto")
            ExtractHwpxRequestDto extractHwpxRequestDto,

            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        // 파일 업로드
        OriginalDocumentVo originalDocumentVo = FileUtil.uploadFile(UPLOAD_PATH, multipartFile);

        HwpxDocument hwpxDocument = chunkHwpxUseCase.chunkHwpxDocument(originalDocumentVo, new ChunkPatternVo(
                extractHwpxRequestDto.getPatterns(), extractHwpxRequestDto.getStopPatterns()));

        // 파일 삭제
        FileUtil.deleteFile(originalDocumentVo.getFullPath());

        // 압축 해제 폴더 삭제
        FileUtil.deleteDirectory(hwpxDocument.getUnZipPath());

        return ResponseEntity.ok(ExtractHwpxResponseDto.builder()
                .lines(hwpxDocument.getLines())
                .passages(hwpxDocument.getPassages())
                .build());
    }

    /**
     * Runtime Exception Handler
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, ?>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
    }
}