package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.PatternDto;
import com.extractor.adapter.in.request.ChunkLawsRequestDto;
import com.extractor.adapter.in.request.ChunkManualsRequestDto;
import com.extractor.adapter.in.request.ChunkRequestDto;
import com.extractor.adapter.in.response.ChunkResponseDto;
import com.extractor.adapter.in.response.ResponseDto;
import com.extractor.adapter.propery.FileProperty;
import com.extractor.application.command.ChunkFileCommand;
import com.extractor.application.command.ChunkLawCommand;
import com.extractor.application.command.ChunkManualCommand;
import com.extractor.application.enums.ExtractType;
import com.extractor.application.enums.SelectType;
import com.extractor.application.usecase.ChunkUseCase;
import com.extractor.application.vo.*;
import com.extractor.global.utils.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Validated
@Tag(name = "ChunkController", description = "청킹 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/chunk")
@RestController
public class ChunkController {

    private final ChunkUseCase chunkUseCase;
    private final FileProperty fileProperty;
    private final FileUtil fileUtil;

    @Operation(summary = "파일 청킹")
    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkFiles(
            @Valid
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
                .patterns(PatternDto.toPatternVo(chunkRequestDto.getPatterns()))
                .antiPatterns(chunkRequestDto.getStopPatterns())
                .maxTokenSize(chunkRequestDto.getMaxTokenSize())
                .overlapSize(chunkRequestDto.getOverlapSize())
                .build();

        List<SourceVo> sourceVos = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            FileVo fileVo = null;

            try {
                fileVo = fileUtil.uploadFile(multipartFile, fileProperty.getTempDir());
                sourceVos.add(chunkUseCase.chunkFileUseCase(ChunkFileCommand.builder()
                        .chunkOption(chunkOptionVo)
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

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("파일 청킹 성공")
                .data(chunkResponseDtos)
                .build());
    }

    @Operation(summary = "법령 청킹")
    @PostMapping(path = "/law")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkLaws(
            @Valid
            @Parameter(name = "chunkLawsRequestDto", description = "법령 청킹 정보", required = true)
            @RequestBody
            ChunkLawsRequestDto chunkLawsRequestDto
    ) {
        ChunkOptionVo chunkOptionVo = ChunkOptionVo.builder()
                .extractType(ExtractType.find(chunkLawsRequestDto.getExtractType()))
                .selectType(SelectType.REGEX)
                .patterns(PatternDto.toPatternVo(chunkLawsRequestDto.getPatterns()))
                .antiPatterns(chunkLawsRequestDto.getExcludeContentTypes())
                .maxTokenSize(chunkLawsRequestDto.getMaxTokenSize())
                .overlapSize(chunkLawsRequestDto.getOverlapSize())
                .build();

        List<SourceVo> sourceVos = chunkLawsRequestDto.getLawIds().stream()
                .map(lawId -> chunkUseCase.chunkLawUseCase(ChunkLawCommand.builder()
                        .chunkOption(chunkOptionVo)
                        .lawId(lawId)
                        .build()))
                .toList();

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

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("법령 청킹 성공")
                .data(chunkResponseDtos)
                .build());
    }

    @Operation(summary = "메뉴얼 청킹")
    @PostMapping(path = "/manual")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkManuals(
            @Valid
            @Parameter(name = "chunkManualsRequestDto", description = "메뉴얼 청킹 정보", required = true)
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

        List<SourceVo> sourceVos = chunkManualsRequestDto.getManualIds().stream()
                .map(manualId -> chunkUseCase.chunkManualUseCase(ChunkManualCommand.builder()
                        .chunkOption(chunkOptionVo)
                        .manualId(manualId)
                        .build()))
                .toList();

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

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("메뉴얼 청킹 성공")
                .data(chunkResponseDtos)
                .build());
    }
}