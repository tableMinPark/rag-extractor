package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.request.ExtractRequestDto;
import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.adapter.in.dto.response.ExtractResponseDto;
import com.extractor.application.usecase.ExtractUseCase;
import com.extractor.application.vo.ExtractVo;
import com.extractor.application.vo.FileVo;
import com.extractor.global.enums.ExtractType;
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
@Tag(name = "ExtractController", description = "파일 추출 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/extract")
@RestController
public class ExtractController {

    private final ExtractUseCase extractUseCase;

    @Operation(summary = "파일 추출")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ExtractResponseDto.class, description = "추출 정보"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<ExtractResponseDto> extractFile(
            @Parameter(name = "extractRequestDto", description = "추출 정보", required = true)
            @RequestPart("requestDto")
            ExtractRequestDto extractRequestDto,
            @Parameter(name = "multipartFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        FileVo fileVo = FileVo.builder().multipartFile(multipartFile).build();
        ExtractType extractType = ExtractType.find(extractRequestDto.getExtractType());

        ExtractVo extractVo = switch (fileVo.getExtension()) {
            case HWP, HWPX -> extractUseCase.extractHwpxDocumentUseCase(fileVo, extractType);
            case PDF -> extractUseCase.extractPdfDocumentUseCase(fileVo);
            default -> throw new RuntimeException("미지원 파일 형식 (HWP, HWPX, PDF 만 지원)");
        };

        log.info("/extract | {} ", extractVo.getName());

        return ResponseEntity.ok(ExtractResponseDto.builder()
                .name(extractVo.getName())
                .extension(extractVo.getExtension().getExt())
                .lines(extractVo.getExtractContents())
                .build());
    }

    @Operation(summary = "파일 텍스트 추출")
    @PostMapping(path = "/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = String.class, description = "추출 텍스트 전문"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<String> extractToText(
            @Parameter(name = "multipartFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        FileVo fileVo = FileVo.builder().multipartFile(multipartFile).build();

        log.info("/extract/text | {} ", fileVo.getOriginalFileName());

        return ResponseEntity.ok(extractUseCase.extractDocumentUseCase(fileVo));
    }
}