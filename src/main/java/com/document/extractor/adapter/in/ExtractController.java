package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.response.ExtractResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.in.enums.Response;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.ExtractFileCommand;
import com.document.extractor.application.command.ExtractFileTextCommand;
import com.document.extractor.application.command.ExtractLawCommand;
import com.document.extractor.application.command.ExtractManualCommand;
import com.document.extractor.application.usecase.ExtractUseCase;
import com.document.extractor.application.vo.DocumentVo;
import com.document.extractor.application.vo.ExtractContentVo;
import com.document.global.utils.FileUtil;
import com.document.global.vo.UploadFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@Tag(name = "ExtractController", description = "추출 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/api/extract")
@RestController
public class ExtractController {

    private final ExtractUseCase extractUseCase;
    private final FileProperty fileProperty;

    @Operation(summary = "파일 추출")
    @PostMapping(path = "/file/{extractType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<ExtractResponseDto>> extractFile(
            @Pattern(regexp = "html|markdown", message = "markdown 과 html 만 지원")
            @Parameter(name = "extractType", description = "추출 정보", required = true)
            @PathVariable("extractType")
            String extractType,
            @Parameter(name = "multipartFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        UploadFile uploadFile = FileUtil.uploadFile(multipartFile, fileProperty.getFileStorePath(), fileProperty.getTempDir());

        try {
            List<ExtractContentVo> extractContentVos = extractUseCase.extractFileUseCase(ExtractFileCommand.builder()
                    .file(uploadFile)
                    .extractType(extractType)
                    .build());

            return ResponseEntity.ok(Response.EXTRACT_FILE_SUCCESS.toResponseDto(ExtractResponseDto.builder()
                    .name(uploadFile.getOriginFileName())
                    .ext(uploadFile.getExt())
                    .lines(extractContentVos)
                    .build()));
        } finally {
            if (uploadFile != null) {
                FileUtil.deleteFile(uploadFile.getUrl());
            }
        }
    }

    @Operation(summary = "파일 텍스트 추출")
    @PostMapping(path = "/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<String>> extractToText(
            @Parameter(name = "multipartFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        UploadFile uploadFile = FileUtil.uploadFile(multipartFile, fileProperty.getFileStorePath(), fileProperty.getTempDir());

        try {
            return ResponseEntity.ok(Response.EXTRACT_TEXT_SUCCESS.toResponseDto(extractUseCase.extractFileTextUseCase(ExtractFileTextCommand.builder()
                    .file(uploadFile)
                    .build())));
        } finally {
            if (uploadFile != null) {
                FileUtil.deleteFile(uploadFile.getUrl());
            }
        }
    }

    @Operation(summary = "법령 문서 추출")
    @GetMapping("/law/{lawId}")
    public ResponseEntity<DocumentVo> extractLaw(
            @Parameter(name = "lawId", description = "법령 ID", required = true)
            @PathVariable("lawId")
            String lawId
    ) {
        return ResponseEntity.ok(extractUseCase.extractLawUseCase(ExtractLawCommand.builder()
                .lawId(lawId)
                .build()));
    }

    @Operation(summary = "메뉴얼 문서 추출")
    @GetMapping("/manual/{manualId}")
    public ResponseEntity<DocumentVo> extractManual(
            @Parameter(name = "manualId", description = "메뉴얼 ID", required = true)
            @PathVariable("manualId")
            String manualId
    ) {
        return ResponseEntity.ok(extractUseCase.extractManualUseCase(ExtractManualCommand.builder()
                .manualId(manualId)
                .build()));
    }
}