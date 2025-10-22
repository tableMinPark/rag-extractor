package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.etc.PatternDto;
import com.extractor.adapter.in.dto.request.ChunkLawsRequestDto;
import com.extractor.adapter.in.dto.response.ChunkDocumentResponseDto;
import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.application.exception.NotFoundDocumentException;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.usecase.DocumentUseCase;
import com.extractor.application.vo.ChunkDocumentVo;
import com.extractor.domain.vo.ChunkPatternVo;
import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
import com.extractor.global.utils.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "BatchChunkController", description = "문서 청킹 배차")
@RequiredArgsConstructor
@RequestMapping("/batch/chunk")
@RestController
public class ChunkBatchController {

    private final ChunkUseCase chunkUseCase;

    private final DocumentUseCase documentUseCase;

    /**
     * 법령 일괄 전처리
     *
     * @param chunkLawsRequestDto 전처리 요청 정보
     */
    @PostMapping(path = "/law")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkDocumentResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    @Operation(summary = "법령 일괄 전처리")
    public ResponseEntity<?> chunkLaws(
            @Parameter(name = "chunkLawsRequestDto", description = "전처리 요청 정보", required = true)
            @RequestBody
            ChunkLawsRequestDto chunkLawsRequestDto
    ) {
        ChunkPatternVo chunkPatternVo = new ChunkPatternVo(
                convertPatternVo(chunkLawsRequestDto.getPatterns()), chunkLawsRequestDto.getExcludeContentTypes(), chunkLawsRequestDto.getMaxTokenSize(), chunkLawsRequestDto.getOverlapSize());

        String version = StringUtil.generateRandomId();

        List<ChunkDocumentResponseDto> chunkDocumentResponseDtos = new ArrayList<>();

        for (Long lawId : chunkLawsRequestDto.getLawIds()) {
            try {
                ChunkDocumentVo chunkDocumentVo = chunkUseCase.chunkLawDocumentUseCase(version, chunkLawsRequestDto.getCategoryCode(), lawId, chunkPatternVo);

                // DB 저장
                if (!chunkDocumentVo.getTrainingDocumentVos().isEmpty()) {
                    documentUseCase.registerDocument(chunkDocumentVo.getOriginalDocumentVo(), chunkDocumentVo.getTrainingDocumentVos());

                    log.info("/chunk/law | {} | {}", lawId, chunkDocumentVo.getTrainingDocumentVos().size());
                } else {
                    log.warn("/chunk/law | {} | not found passage", lawId);
                }

                chunkDocumentResponseDtos.add(ChunkDocumentResponseDto.builder()
                        .originalId(chunkDocumentVo.getOriginalDocumentVo().getOriginalId())
                        .version(chunkDocumentVo.getOriginalDocumentVo().getVersion())
                        .name(chunkDocumentVo.getOriginalDocumentVo().getName())
                        .docType(chunkDocumentVo.getOriginalDocumentVo().getDocType())
                        .categoryCode(chunkDocumentVo.getOriginalDocumentVo().getCategoryCode())
                        .chunkCount(chunkDocumentVo.getTrainingDocumentVos().size())
                        // .chunks(chunkDocumentVo.getTrainingDocumentVos())
                        // .chunkInfo(chunkPatternVo)
                        .build());

            } catch (NotFoundDocumentException e) {

                log.info("/chunk/law | {} | not found law in database", lawId);

            } catch (RuntimeException e) {

                log.error("/chunk/law | {} | {}", lawId, e.getMessage());

                return ResponseEntity.internalServerError().body(ErrorResponseDto.builder()
                        .message(e.getMessage())
                        .stackTrace(e.getStackTrace())
                        .build());
            }
        }

        return ResponseEntity.ok(chunkDocumentResponseDtos);
    }

    /**
     * 패턴 검증
     *
     * @param patternDtos 패턴 Dto
     * @return 검증 이후 패턴 Vo 목록
     */
    private List<PatternVo> convertPatternVo(List<PatternDto> patternDtos) {
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