package com.genai.extractor.controller;

import com.genai.extractor.config.FileProperty;
import com.genai.extractor.controller.dto.response.ExtractResponseDto;
import com.genai.extractor.vo.ExtractContentVo;
import com.genai.extractor.enums.ExtractType;
import com.genai.extractor.service.ExtractService;
import com.genai.extractor.repository.entity.FileDetailEntity;
import com.genai.common.utils.FileUtil;
import com.genai.common.vo.UploadFile;
import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
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
@RequestMapping("/extract")
@RestController
public class ExtractController {

    private final ExtractService extractService;
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
            ExtractType extractTypeEnum = ExtractType.find(extractType);

            FileDetailEntity fileDetail = FileDetailEntity.builder()
                    .originFileName(uploadFile.getOriginFileName())
                    .fileName(uploadFile.getFileName())
                    .url(uploadFile.getUrl())
                    .filePath(uploadFile.getFilePath())
                    .fileSize(uploadFile.getFileSize())
                    .ext(uploadFile.getExt())
                    .build();

            List<ExtractContentVo> extractContentVos = extractService.extractFile(fileDetail, extractTypeEnum.getCode())
                    .getDocumentContents().stream()
                    .map(documentContent -> ExtractContentVo.builder()
                            .type(documentContent.getType().name())
                            .content(documentContent.getContext())
                            .build())
                    .toList();

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
            FileDetailEntity fileDetail = FileDetailEntity.builder()
                    .originFileName(uploadFile.getOriginFileName())
                    .fileName(uploadFile.getFileName())
                    .url(uploadFile.getUrl())
                    .filePath(uploadFile.getFilePath())
                    .fileSize(uploadFile.getFileSize())
                    .ext(uploadFile.getExt())
                    .build();

            return ResponseEntity.ok(Response.EXTRACT_TEXT_SUCCESS.toResponseDto(extractService.extractText(fileDetail)));
        } finally {
            if (uploadFile != null) {
                FileUtil.deleteFile(uploadFile.getUrl());
            }
        }
    }
}
