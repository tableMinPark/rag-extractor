package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.etc.PatternDto;
import com.extractor.adapter.in.dto.request.ChunkRequestDto;
import com.extractor.adapter.in.dto.response.ChunkResponseDto;
import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.application.vo.PassageDocumentVo;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.domain.vo.pattern.PatternVo;
import com.extractor.domain.vo.pattern.PrefixVo;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "ChunkController", description = "패시지 셀렉트 (청킹)")
@RequiredArgsConstructor
@RequestMapping("/chunk")
@RestController
public class ChunkController {

    private final ChunkUseCase chunkUseCase;

    /**
     * 문서 전처리
     * @param chunkRequestDto 전처리 요청 정보
     * @param multipartFile 업로드 파일
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    @Operation(summary = "전처리 전처리")
    public ResponseEntity<?> extractHwpx(
            @Parameter(name = "chunkRequestDto", description = "전처리 요청 정보", required = true)
            @RequestPart("requestDto")
            ChunkRequestDto chunkRequestDto,

            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            MultipartFile multipartFile
    ) {
        try {
            FileExtension extension = FileExtension.find(multipartFile.getContentType());

            ChunkPatternVo chunkPatternVo = new ChunkPatternVo(
                    convertPatternVo(chunkRequestDto.getPatterns()), chunkRequestDto.getStopPatterns());

            List<PassageDocumentVo> passages;
            switch (extension) {
                case HWP, HWPX -> passages =
                        chunkUseCase.chunkHwpxDocumentUseCase(new OriginalDocumentVo(multipartFile), chunkPatternVo);
                case PDF -> passages =
                        chunkUseCase.chunkPdfDocumentUseCase(new OriginalDocumentVo(multipartFile), chunkPatternVo);
                default -> throw new RuntimeException("미지원 파일 형식 (HWP, HWPX, PDF 만 지원)");
            }

            log.info("/chunk | {} ", multipartFile.getOriginalFilename());

            return ResponseEntity.ok(ChunkResponseDto.builder()
                    .chunkInfo(chunkPatternVo)
                    .passages(passages)
                    .build());

        } catch (RuntimeException e) {

            log.error("{} | {}", multipartFile.getOriginalFilename(), e.getMessage());

            return ResponseEntity.internalServerError().body(ErrorResponseDto.builder()
                    .message(e.getMessage())
                    .stackTrace(e.getStackTrace())
                    .build());
        }
    }

    /**
     * 패턴 검증
     * @param patternDtos 패턴 Dto
     * @return 검증 이후 패턴 Vo 목록
     */
    List<PatternVo> convertPatternVo(List<PatternDto> patternDtos) {
        List<PatternVo> patterns = new ArrayList<>();
        int maxTokenSize = 0;

        for (PatternDto patternDto : patternDtos) {

            List<PrefixVo> prefixes = patternDto.getPrefixes().stream()
                    .map(prefixDto -> new PrefixVo(prefixDto.getPrefix(), prefixDto.getIsDeleting()))
                    .toList();

            patterns.add(PatternVo.builder()
                    .prefixes(prefixes)
                    .tokenSize(maxTokenSize > patternDto.getTokenSize()
                            ? maxTokenSize
                            : patternDto.getTokenSize())
                    .build());

            maxTokenSize = Math.max(maxTokenSize, patternDto.getTokenSize());
        }
        return patterns;
    }
}