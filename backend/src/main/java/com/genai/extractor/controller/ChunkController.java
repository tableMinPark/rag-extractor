package com.genai.extractor.controller;

import com.genai.extractor.config.FileProperty;
import com.genai.extractor.controller.dto.request.*;
import com.genai.extractor.controller.dto.response.ChunkResponseDto;
import com.genai.extractor.controller.dto.response.GetChunkResponseDto;
import com.genai.extractor.dto.PatternDto;
import com.genai.extractor.vo.ChunkResultVo;
import com.genai.extractor.vo.ChunkVo;
import com.genai.extractor.service.ChunkService;
import com.genai.global.dto.PageResponseDto;
import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import com.genai.global.wrapper.PageWrapper;
import com.genai.common.utils.FileUtil;
import com.genai.common.vo.UploadFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Validated
@Tag(name = "ChunkController", description = "청킹 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/chunk")
public class ChunkController {

    private final ChunkService chunkService;
    private final FileProperty fileProperty;

    @Operation(summary = "파일 청킹")
    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkFiles(
            @Valid
            @Parameter(name = "chunkFilesRequestDto", description = "파일 청킹 정보", required = true)
            @RequestPart("requestDto")
            ChunkFilesRequestDto chunkFilesRequestDto,
            @Parameter(name = "uploadFile", description = "업로드 파일", required = true)
            @RequestPart("uploadFile")
            List<MultipartFile> multipartFiles
    ) {
        List<ChunkResultVo> chunkResultVos = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            UploadFile uploadFile = FileUtil.uploadFile(multipartFile, fileProperty.getFileStorePath(), fileProperty.getTempDir());

            try {
                chunkResultVos.add(chunkService.chunkFile(
                        chunkFilesRequestDto.getExtractType(),
                        chunkFilesRequestDto.getSelectType(),
                        PatternDto.toPatternVo(chunkFilesRequestDto.getPatterns()),
                        chunkFilesRequestDto.getStopPatterns(),
                        chunkFilesRequestDto.getMaxTokenSize(),
                        chunkFilesRequestDto.getOverlapSize(),
                        uploadFile));
            } finally {
                if (uploadFile != null) {
                    FileUtil.deleteFile(uploadFile.getUrl());
                }
            }
        }

        List<ChunkResponseDto> chunkResponseDtos = chunkResultVos.stream()
                .map(chunkResultVo -> ChunkResponseDto.builder()
                        .isConvertError(chunkResultVo.getIsConvertError())
                        .previousPassages(chunkResultVo.getPreviousPassages())
                        .currentPassages(chunkResultVo.getCurrentPassages())
                        .chunks(chunkResultVo.getChunks())
                        .build())
                .toList();

        return ResponseEntity.ok(Response.CHUNK_FILES_SUCCESS.toResponseDto(chunkResponseDtos));
    }

    @Operation(summary = "원격 문서 청킹")
    @PostMapping(path = "/repo")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkRepos(
            @Valid
            @Parameter(name = "chunkReposRequestDto", description = "원격 문서 청킹 정보", required = true)
            @RequestBody
            ChunkReposRequestDto chunkReposRequestDto
    ) {
        List<ChunkResultVo> chunkResultVos = chunkReposRequestDto.getUris().stream()
                .map(uri -> chunkService.chunkRepo(
                        chunkReposRequestDto.getExtractType(),
                        chunkReposRequestDto.getSelectType(),
                        PatternDto.toPatternVo(chunkReposRequestDto.getPatterns()),
                        chunkReposRequestDto.getStopPatterns(),
                        chunkReposRequestDto.getMaxTokenSize(),
                        chunkReposRequestDto.getOverlapSize(),
                        uri))
                .toList();

        List<ChunkResponseDto> chunkResponseDtos = chunkResultVos.stream()
                .map(chunkResultVo -> ChunkResponseDto.builder()
                        .previousPassages(chunkResultVo.getPreviousPassages())
                        .currentPassages(chunkResultVo.getCurrentPassages())
                        .chunks(chunkResultVo.getChunks())
                        .build())
                .toList();

        return ResponseEntity.ok(Response.CHUNK_REPOS_SUCCESS.toResponseDto(chunkResponseDtos));
    }

    @Operation(summary = "대상 문서 청킹")
    @PostMapping(path = "/source")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkSources(
            @Valid
            @Parameter(name = "chunkSourcesRequestDto", description = "대상 문서 청킹 정보", required = true)
            @RequestBody
            ChunkSourcesRequestDto chunkSourcesRequestDto
    ) {
        List<ChunkResultVo> chunkResultVos = chunkSourcesRequestDto.getSourceIds().stream()
                .map(chunkService::chunkSource)
                .toList();

        List<ChunkResponseDto> chunkResponseDtos = chunkResultVos.stream()
                .map(chunkResultVo -> ChunkResponseDto.builder()
                        .isConvertError(chunkResultVo.getIsConvertError())
                        .previousPassages(chunkResultVo.getPreviousPassages())
                        .currentPassages(chunkResultVo.getCurrentPassages())
                        .chunks(chunkResultVo.getChunks())
                        .build())
                .toList();

        return ResponseEntity.ok(Response.CHUNK_SOURCE_SUCCESS.toResponseDto(chunkResponseDtos));
    }

    @Operation(summary = "청크 등록 (패시지 ID 기준)")
    @PostMapping
    public ResponseEntity<ResponseDto<?>> createChunk(
            @Valid
            @Parameter(name = "createChunkRequestDto", description = "청크 등록 정보", required = true)
            @RequestBody
            CreateChunkRequestDto createChunkRequestDto
    ) {
        chunkService.createChunk(
                createChunkRequestDto.getPassageId(),
                createChunkRequestDto.getTitle(),
                createChunkRequestDto.getSubTitle(),
                createChunkRequestDto.getThirdTitle(),
                createChunkRequestDto.getContent(),
                createChunkRequestDto.getSubContent());

        return ResponseEntity.ok(Response.CREATE_CHUNK_SUCCESS.toResponseDto());
    }

    @Operation(summary = "청크 조회 (청크 ID 기준)")
    @GetMapping(path = "/{chunkId}")
    public ResponseEntity<ResponseDto<GetChunkResponseDto>> getChunk(@PathVariable("chunkId") Long chunkId) {
        ChunkVo chunkVo = chunkService.getChunk(chunkId);
        GetChunkResponseDto getChunkResponseDto = GetChunkResponseDto.of(chunkVo);
        return ResponseEntity.ok(Response.GET_CHUNK_SUCCESS.toResponseDto(getChunkResponseDto));
    }

    @Operation(summary = "청크 목록 조회 (패시지 ID 기준, 페이징)")
    @GetMapping
    public ResponseEntity<ResponseDto<PageResponseDto<GetChunkResponseDto>>> getChunks(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("passageId") long passageId
    ) {
        PageWrapper<ChunkVo> chunkVoPageWrapper = chunkService.getChunks(page, size, passageId);

        PageResponseDto<GetChunkResponseDto> pageResponseDto = PageResponseDto.<GetChunkResponseDto>builder()
                .content(GetChunkResponseDto.toList(chunkVoPageWrapper.getContent()))
                .isLast(chunkVoPageWrapper.isLast())
                .pageNo(chunkVoPageWrapper.getPageNo())
                .pageSize(chunkVoPageWrapper.getPageSize())
                .totalCount(chunkVoPageWrapper.getTotalCount())
                .totalPages(chunkVoPageWrapper.getTotalPages())
                .build();

        return ResponseEntity.ok(Response.GET_CHUNKS_SUCCESS.toResponseDto(pageResponseDto));
    }

    @Operation(summary = "청크 수정")
    @PutMapping(path = "/{chunkId}")
    public ResponseEntity<ResponseDto<?>> updateChunk(
            @Parameter(name = "chunkId", description = "청크 ID", required = true)
            @PathVariable("chunkId")
            Long chunkId,
            @Valid
            @Parameter(name = "updateChunkRequestDto", description = "청크 수정 정보", required = true)
            @RequestBody
            UpdateChunkRequestDto updateChunkRequestDto
    ) {
        chunkService.updateChunk(
                chunkId,
                updateChunkRequestDto.getTitle(),
                updateChunkRequestDto.getSubTitle(),
                updateChunkRequestDto.getThirdTitle(),
                updateChunkRequestDto.getContent(),
                updateChunkRequestDto.getSubContent());

        return ResponseEntity.ok(Response.UPDATE_CHUNK_SUCCESS.toResponseDto());
    }

    @Operation(summary = "청크 삭제")
    @DeleteMapping(path = "/{chunkId}")
    public ResponseEntity<ResponseDto<?>> deleteChunk(@PathVariable("chunkId") Long chunkId) {
        chunkService.deleteChunk(chunkId);
        return ResponseEntity.ok(Response.DELETE_CHUNK_SUCCESS.toResponseDto());
    }
}
