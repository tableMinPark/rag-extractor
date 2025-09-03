package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.ExtractRequestDto;
import com.extractor.adapter.in.dto.ExtractResponseDto;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.global.enums.FileExtension;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    private final ChunkUseCase chunkUseCase;

    /**
     * HWP 문서 전처리
     * @param extractRequestDto 전처리 요청 정보
     * @param multipartFile 업로드 파일
     */
    @PostMapping(path = "/hwp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ExtractResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = Map.class))}),
    })
    @Operation(summary = "한글 문서 전처리 정규식 테스트")
    public ResponseEntity<ExtractResponseDto> extractHwpx(
            @Parameter(name = "extractRequestDto", description = "전처리 요청 정보", required = true)
            @RequestPart("requestDto")
            ExtractRequestDto extractRequestDto,

            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        // 한글 파일 체크
        String extension = multipartFile.getContentType();
        if (!FileExtension.HWP.equals(FileExtension.find(extension)) && !FileExtension.HWPX.equals(FileExtension.find(extension))) {
            throw new RuntimeException("possible hwp or hwpx only");
        }

        HwpxDocument hwpxDocument = chunkUseCase.chunkHwpxDocument(
                new OriginalDocumentVo(multipartFile),
                new ChunkPatternVo(extractRequestDto.getTokenSize(), extractRequestDto.getPatterns(), extractRequestDto.getStopPatterns()));

        return ResponseEntity.ok(ExtractResponseDto.builder()
                .lines(hwpxDocument.getLines())
                .passages(hwpxDocument.getPassages())
                .build());
    }

    /**
     * PDF 문서 전처리
     * @param extractRequestDto 전처리 요청 정보 Dto
     * @param multipartFile 업로드 파일
     */
    @PostMapping(path = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ExtractResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = Map.class))}),
    })
    @Operation(summary = "PDF 문서 전처리 정규식 테스트")
    public ResponseEntity<ExtractResponseDto> extractPdf(
            @Parameter(name = "extractRequestDto", description = "전처리 요청 정보", required = true)
            @RequestPart("requestDto")
            ExtractRequestDto extractRequestDto,

            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        // PDF 파일 체크
        String extension = multipartFile.getContentType();
        if (!FileExtension.PDF.equals(FileExtension.find(extension))) {
            throw new RuntimeException("possible pdf only");
        }

        PdfDocument pdfDocument = chunkUseCase.chunkPdfDocument(
                new OriginalDocumentVo(multipartFile),
                new ChunkPatternVo(extractRequestDto.getTokenSize(), extractRequestDto.getPatterns(), extractRequestDto.getStopPatterns()));

        return ResponseEntity.ok(ExtractResponseDto.builder()
                .lines(pdfDocument.getLines())
                .passages(pdfDocument.getPassages())
                .build());
    }

    /**
     * Runtime Exception Handler
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, ?>> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();        // TODO: 디버깅 이후 삭제 예정
        return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
    }
}