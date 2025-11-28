package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.etc.PatternDto;
import com.extractor.adapter.in.dto.request.ChunkBatchRequestDto;
import com.extractor.adapter.in.dto.request.ChunkLawsBatchRequestDto;
import com.extractor.adapter.in.dto.request.ChunkManualsBatchRequestDto;
import com.extractor.adapter.in.dto.response.ChunkBatchResponseDto;
import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.usecase.SourceUseCase;
import com.extractor.application.vo.*;
import com.extractor.global.enums.ExtractType;
import com.extractor.global.enums.SelectType;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Tag(name = "ChunkBatchController", description = "청킹 배치 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/batch/chunk")
@RestController
public class ChunkBatchController {

    private final ChunkUseCase chunkUseCase;
    private final SourceUseCase sourceUseCase;

    @Operation(summary = "파일 청킹 배치")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkBatchResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<List<ChunkBatchResponseDto>> chunkFiles(
            @Parameter(name = "chunkRequestDto", description = "파일 청킹 정보", required = true)
            @RequestPart("requestDto")
            ChunkBatchRequestDto chunkRequestDto,
            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            List<MultipartFile> multipartFiles
    ) {
        ChunkOptionVo chunkOptionVo = ChunkOptionVo.builder()
                .extractType(ExtractType.find(chunkRequestDto.getExtractType()))
                .selectType(SelectType.find(chunkRequestDto.getSelectType()))
                .patterns(PatternDto.convertPatternVo(chunkRequestDto.getPatterns()))
                .antiPatterns(chunkRequestDto.getStopPatterns())
                .maxTokenSize(chunkRequestDto.getMaxTokenSize())
                .overlapSize(chunkRequestDto.getOverlapSize())
                .isExtractTitle(chunkRequestDto.isExtractTitle())
                .build();

        List<SourceVo> sourceVos = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            FileVo fileVo = FileVo.builder().multipartFile(multipartFile).build();

            SourceVo sourceVo = chunkUseCase.chunkFileUseCase(chunkOptionVo, fileVo);
            sourceVos.add(sourceVo);

            log.info("/batch/chunk | {} ", sourceVo.getName());
        }

        SourceOptionVo sourceOptionVo = SourceOptionVo.builder()
                .version(StringUtil.generateRandomId())
                .collectionId(chunkRequestDto.getCollectionId())
                .categoryCode(chunkRequestDto.getCategoryCode())
                .build();

        // DB 저장
        sourceUseCase.createSource(sourceOptionVo, sourceVos);

        List<ChunkBatchResponseDto> chunkResponseDtos = sourceVos.stream()
                .map(sourceVo -> {
                    List<PassageVo> passageVos = sourceVo.getPassageVos();
                    List<ChunkVo> chunkVos = new ArrayList<>();

                    for (PassageVo passageVo : sourceVo.getPassageVos()) {
                        chunkVos.addAll(passageVo.getChunkVos());
                    }

                    return ChunkBatchResponseDto.builder()
                            .source(sourceVo)
                            .passages(passageVos)
                            .chunks(chunkVos)
                            .build();

                })
                .toList();

        return ResponseEntity.ok(chunkResponseDtos);
    }

    @Operation(summary = "법령 청킹 배치")
    @PostMapping(path = "/law")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkBatchResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<List<ChunkBatchResponseDto>> chunkLaws(
            @Parameter(name = "chunkLawsRequestDto", description = "법령 청킹 정보", required = true)
            @RequestBody
            ChunkLawsBatchRequestDto chunkLawsRequestDto
    ) {
        ChunkOptionVo chunkOptionVo = ChunkOptionVo.builder()
                .extractType(ExtractType.find(chunkLawsRequestDto.getExtractType()))
                .selectType(SelectType.REGEX)
                .patterns(PatternDto.convertPatternVo(chunkLawsRequestDto.getPatterns()))
                .antiPatterns(chunkLawsRequestDto.getExcludeContentTypes())
                .maxTokenSize(chunkLawsRequestDto.getMaxTokenSize())
                .overlapSize(chunkLawsRequestDto.getOverlapSize())
                .build();

        List<SourceVo> sourceVos = new ArrayList<>();
        for (Long lawId : chunkLawsRequestDto.getLawIds()) {
            SourceVo sourceVo = chunkUseCase.chunkLawUseCase(chunkOptionVo, lawId);
            sourceVos.add(sourceVo);

            log.info("/batch/chunk/law | {} ", sourceVo.getName());
        }

        SourceOptionVo sourceOptionVo = SourceOptionVo.builder()
                .version(StringUtil.generateRandomId())
                .collectionId(chunkLawsRequestDto.getCollectionId())
                .categoryCode(chunkLawsRequestDto.getCategoryCode())
                .build();

        // DB 저장
        sourceUseCase.createSource(sourceOptionVo, sourceVos);

        List<ChunkBatchResponseDto> chunkResponseDtos = sourceVos.stream()
                .map(sourceVo -> {
                    List<PassageVo> passageVos = sourceVo.getPassageVos();
                    List<ChunkVo> chunkVos = new ArrayList<>();

                    for (PassageVo passageVo : sourceVo.getPassageVos()) {
                        chunkVos.addAll(passageVo.getChunkVos());
                    }

                    return ChunkBatchResponseDto.builder()
                            .source(sourceVo)
                            .passages(passageVos)
                            .chunks(chunkVos)
                            .build();

                })
                .toList();

        return ResponseEntity.ok(chunkResponseDtos);
    }

    @Operation(summary = "메뉴얼 청킹 배치")
    @PostMapping(path = "/manual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkBatchResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<List<ChunkBatchResponseDto>> chunkManual(
            @Parameter(name = "chunkManualsRequestDto", description = "메뉴얼 청킹 정보", required = true)
            @RequestBody
            ChunkManualsBatchRequestDto chunkManualsRequestDto
    ) {
        ChunkOptionVo chunkOptionVo = ChunkOptionVo.builder()
                .extractType(ExtractType.find(chunkManualsRequestDto.getExtractType()))
                .selectType(SelectType.NONE)
                .patterns(Collections.emptyList())
                .antiPatterns(Collections.emptyList())
                .maxTokenSize(chunkManualsRequestDto.getMaxTokenSize())
                .overlapSize(chunkManualsRequestDto.getOverlapSize())
                .build();

        List<SourceVo> sourceVos = new ArrayList<>();
        for (Long manualId : chunkManualsRequestDto.getManualIds()) {
            SourceVo sourceVo = chunkUseCase.chunkManualUseCase(chunkOptionVo, manualId);
            sourceVos.add(sourceVo);

            log.info("/batch/chunk/manual | {} ", sourceVo.getName());
        }

        SourceOptionVo sourceOptionVo = SourceOptionVo.builder()
                .version(StringUtil.generateRandomId())
                .collectionId(chunkManualsRequestDto.getCollectionId())
                .categoryCode(chunkManualsRequestDto.getCategoryCode())
                .build();

        // DB 저장
        sourceUseCase.createSource(sourceOptionVo, sourceVos);

        List<ChunkBatchResponseDto> chunkResponseDtos = sourceVos.stream()
                .map(sourceVo -> {
                    List<PassageVo> passageVos = sourceVo.getPassageVos();
                    List<ChunkVo> chunkVos = new ArrayList<>();

                    for (PassageVo passageVo : sourceVo.getPassageVos()) {
                        chunkVos.addAll(passageVo.getChunkVos());
                    }

                    return ChunkBatchResponseDto.builder()
                            .source(sourceVo)
                            .passages(passageVos)
                            .chunks(chunkVos)
                            .build();

                })
                .toList();

        return ResponseEntity.ok(chunkResponseDtos);
    }
}