package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.ExtractHwpxRequestDto;
import com.extractor.adapter.in.dto.ExtractHwpxResponseDto;
import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.usecase.ChunkHwpxUseCase;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/extract")
@RequiredArgsConstructor
public class ExtractController {

    private final ChunkHwpxUseCase chunkHwpxUseCase;

    @Value("${env.upload-path}")
    private String UPLOAD_PATH;

    @PostMapping
    public ResponseEntity<ExtractHwpxResponseDto> extractHwpx(
            @RequestPart("requestDto") ExtractHwpxRequestDto extractHwpxRequestDto,
            @RequestPart("uploadFile") MultipartFile multipartFile
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
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
