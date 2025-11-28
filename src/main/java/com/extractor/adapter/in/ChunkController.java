package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.etc.PatternDto;
import com.extractor.adapter.in.dto.request.ChunkLawsRequestDto;
import com.extractor.adapter.in.dto.request.ChunkManualsRequestDto;
import com.extractor.adapter.in.dto.request.ChunkRequestDto;
import com.extractor.adapter.in.dto.response.ChunkResponseDto;
import com.extractor.adapter.in.dto.response.ErrorResponseDto;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.*;
import com.extractor.global.enums.ExtractType;
import com.extractor.global.enums.SelectType;
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
@Tag(name = "ChunkController", description = "청킹 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/chunk")
@RestController
public class ChunkController {

    private final ChunkUseCase chunkUseCase;

    @Operation(summary = "파일 청킹")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<List<ChunkResponseDto>> chunkFiles(
            @Parameter(name = "chunkRequestDto", description = "파일 청킹 정보", required = true)
            @RequestPart("requestDto")
            ChunkRequestDto chunkRequestDto,
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

            log.info("/chunk | {} ", sourceVo.getName());
        }

        List<ChunkResponseDto> chunkResponseDtos = sourceVos.stream()
                .map(sourceVo -> {
                    List<PassageVo> passageVos = sourceVo.getPassageVos();
                    List<ChunkVo> chunkVos = new ArrayList<>();

                    for (PassageVo passageVo : sourceVo.getPassageVos()) {
                        chunkVos.addAll(passageVo.getChunkVos());
                    }

                    return ChunkResponseDto.builder()
                            .source(sourceVo)
                            .passages(passageVos)
                            .chunks(chunkVos)
                            .build();

                })
                .toList();

        return ResponseEntity.ok(chunkResponseDtos);
    }

    @Operation(summary = "법령 청킹")
    @PostMapping(path = "/law")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<List<ChunkResponseDto>> chunkLaws(
            @Parameter(name = "chunkLawsRequestDto", description = "법령 청킹 정보", required = true)
            @RequestBody
            ChunkLawsRequestDto chunkLawsRequestDto
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

            log.info("/chunk/law | {} ", sourceVo.getName());
        }

        List<ChunkResponseDto> chunkResponseDtos = sourceVos.stream()
                .map(sourceVo -> {
                    List<PassageVo> passageVos = sourceVo.getPassageVos();
                    List<ChunkVo> chunkVos = new ArrayList<>();

                    for (PassageVo passageVo : sourceVo.getPassageVos()) {
                        chunkVos.addAll(passageVo.getChunkVos());
                    }

                    return ChunkResponseDto.builder()
                            .source(sourceVo)
                            .passages(passageVos)
                            .chunks(chunkVos)
                            .build();

                })
                .toList();

        return ResponseEntity.ok(chunkResponseDtos);
    }

    @Operation(summary = "메뉴얼 청킹")
    @PostMapping(path = "/manual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = ChunkResponseDto.class, description = "전처리 응답"))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class, description = "에러 응답"))}),
    })
    public ResponseEntity<List<ChunkResponseDto>> chunkManuals(
            @Parameter(name = "chunkManualRequestDto", description = "메뉴얼 청킹 정보", required = true)
            @RequestBody
            ChunkManualsRequestDto chunkManualsRequestDto
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

            log.info("/chunk/manual | {} ", sourceVo.getName());
        }

        List<ChunkResponseDto> chunkResponseDtos = sourceVos.stream()
                .map(sourceVo -> {
                    List<PassageVo> passageVos = sourceVo.getPassageVos();
                    List<ChunkVo> chunkVos = new ArrayList<>();

                    for (PassageVo passageVo : sourceVo.getPassageVos()) {
                        chunkVos.addAll(passageVo.getChunkVos());
                    }

                    return ChunkResponseDto.builder()
                            .source(sourceVo)
                            .passages(passageVos)
                            .chunks(chunkVos)
                            .build();

                })
                .toList();

        return ResponseEntity.ok(chunkResponseDtos);
    }
}