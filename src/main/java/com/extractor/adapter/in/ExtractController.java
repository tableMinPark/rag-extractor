package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.adapter.in.dto.response.ExtractResponseDto;
import com.extractor.application.usecase.ExtractUseCase;
import com.extractor.application.vo.ExtractDocumentVo;
import com.extractor.domain.vo.FileDocumentVo;
import com.extractor.global.enums.FileExtension;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "ExtractController", description = "문서 추출")
@RequiredArgsConstructor
@RequestMapping("/extract")
@RestController
public class ExtractController {

    private final ExtractUseCase extractUseCase;

    /**
     * 문서 추출
     *
     * @param multipartFile 업로드 파일
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ExtractResponseDto.class, description = "추출 정보"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    @Operation(summary = "문서 추출")
    public ResponseEntity<?> extractDocument(
            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        try {
            FileExtension extension = FileExtension.find(multipartFile.getContentType());

            ExtractDocumentVo extractDocumentVo;
            switch (extension) {
                case HWP, HWPX -> extractDocumentVo =
                        extractUseCase.extractHwpxDocumentUseCase(new FileDocumentVo(multipartFile));
                case PDF -> extractDocumentVo =
                        extractUseCase.extractPdfDocumentUseCase(new FileDocumentVo(multipartFile));
                default -> throw new RuntimeException("미지원 파일 형식 (HWP, HWPX, PDF 만 지원)");
            }

            log.info("/extract | {} ", multipartFile.getOriginalFilename());

            return ResponseEntity.ok(ExtractResponseDto.builder()
                    .name(extractDocumentVo.getName())
                    .extension(extractDocumentVo.getExtension().getSimpleExtension())
                    .lines(extractDocumentVo.getExtractContents())
                    .build());

        } catch (RuntimeException e) {

            log.error("/extract | {} | {}", multipartFile.getOriginalFilename(), e.getMessage());

            return ResponseEntity.internalServerError().body(ErrorResponseDto.builder()
                    .message(e.getMessage())
                    .stackTrace(e.getStackTrace())
                    .build());
        }
    }

    /**
     * 문서 텍스트 추출
     *
     * @param multipartFile 업로드 파일
     */
    @PostMapping(path = "/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = String.class, description = "추출 텍스트 전문"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    @Operation(summary = "문서 텍스트 추출")
    public ResponseEntity<?> extractToText(
            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        try {
            String text = extractUseCase.extractDocumentUseCase(new FileDocumentVo(multipartFile));

            log.info("/extract/text | {} ", multipartFile.getOriginalFilename());

            return ResponseEntity.ok(text);
        } catch (RuntimeException e) {

            log.error("/extract/text | {} | {}", multipartFile.getOriginalFilename(), e.getMessage());

            return ResponseEntity.internalServerError().body(ErrorResponseDto.builder()
                    .message(e.getMessage())
                    .stackTrace(e.getStackTrace())
                    .build());
        }
    }
}