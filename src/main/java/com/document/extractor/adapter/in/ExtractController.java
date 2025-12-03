package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.response.ExtractResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.ExtractFileCommand;
import com.document.extractor.application.command.ExtractFileTextCommand;
import com.document.extractor.application.usecase.ExtractUseCase;
import com.document.extractor.application.utils.FileUtil;
import com.document.extractor.application.vo.ExtractContentVo;
import com.document.extractor.application.vo.FileVo;
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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Validated
@Tag(name = "ExtractController", description = "추출 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/extract")
@RestController
public class ExtractController {

    private final ExtractUseCase extractUseCase;
    private final FileProperty fileProperty;
    private final FileUtil fileUtil;

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
        FileVo fileVo = null;

        try {
            fileVo = fileUtil.uploadFile(multipartFile, fileProperty.getTempDir());

            List<ExtractContentVo> extractContentVos = extractUseCase.extractFileUseCase(ExtractFileCommand.builder()
                    .file(fileVo)
                    .extractType(extractType)
                    .build());

            return ResponseEntity.ok(ResponseDto.<ExtractResponseDto>builder()
                    .message("파일 추출 성공")
                    .data(ExtractResponseDto.builder()
                            .name(fileVo.getOriginFileName())
                            .ext(fileVo.getExt())
                            .lines(extractContentVos)
                            .build())
                    .build());

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패");
        } finally {
            if (fileVo != null) {
                fileUtil.deleteFile(Paths.get(fileVo.getUrl()));
            }
        }
    }

    @Operation(summary = "파일 텍스트 추출")
    @PostMapping(path = "/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> extractToText(
            @Parameter(name = "multipartFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        FileVo fileVo = null;

        try {
            fileVo = fileUtil.uploadFile(multipartFile, fileProperty.getTempDir());

            return ResponseEntity.ok(extractUseCase.extractFileTextUseCase(ExtractFileTextCommand.builder()
                    .file(fileVo)
                    .build()));

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패");
        } finally {
            if (fileVo != null) {
                fileUtil.deleteFile(Paths.get(fileVo.getUrl()));
            }
        }
    }
}