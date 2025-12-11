package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import com.document.extractor.adapter.in.dto.request.*;
import com.document.extractor.adapter.in.dto.response.*;
import com.document.extractor.adapter.in.enums.Response;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.*;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.vo.ChunkResultVo;
import com.document.extractor.application.vo.ChunkVo;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.wrapper.PageWrapper;
import com.document.global.utils.FileUtil;
import com.document.global.vo.UploadFile;
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
public class ChunkController {

    private final ChunkUseCase chunkUseCase;
    private final FileProperty fileProperty;

    @Operation(summary = "파일 청킹")
    @PostMapping(path = "/chunk/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
                chunkResultVos.add(chunkUseCase.chunkFileUseCase(ChunkFileCommand.builder()
                        .extractType(chunkFilesRequestDto.getExtractType())
                        .selectType(chunkFilesRequestDto.getSelectType())
                        .patterns(PatternDto.toPatternVo(chunkFilesRequestDto.getPatterns()))
                        .stopPatterns(chunkFilesRequestDto.getStopPatterns())
                        .maxTokenSize(chunkFilesRequestDto.getMaxTokenSize())
                        .overlapSize(chunkFilesRequestDto.getOverlapSize())
                        .file(uploadFile)
                        .build()));
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
    @PostMapping(path = "/chunk/repo")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkRepos(
            @Valid
            @Parameter(name = "chunkReposRequestDto", description = "원격 문서 청킹 정보", required = true)
            @RequestBody
            ChunkReposRequestDto chunkReposRequestDto
    ) {
        List<ChunkResultVo> chunkResultVos = chunkReposRequestDto.getUris().stream()
                .map(uri -> chunkUseCase.chunkRepoUseCase(ChunkRepoCommand.builder()
                        .extractType(chunkReposRequestDto.getExtractType())
                        .selectType(chunkReposRequestDto.getSelectType())
                        .patterns(PatternDto.toPatternVo(chunkReposRequestDto.getPatterns()))
                        .stopPatterns(chunkReposRequestDto.getStopPatterns())
                        .maxTokenSize(chunkReposRequestDto.getMaxTokenSize())
                        .overlapSize(chunkReposRequestDto.getOverlapSize())
                        .uri(uri)
                        .build()))
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
    @PostMapping(path = "/chunk/source")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkSources(
            @Valid
            @Parameter(name = "chunkSourcesRequestDto", description = "대상 문서 청킹 정보", required = true)
            @RequestBody
            ChunkSourcesRequestDto chunkSourcesRequestDto
    ) {
        List<ChunkResultVo> chunkResultVos = chunkSourcesRequestDto.getSourceIds().stream()
                .map(sourceId -> chunkUseCase.chunkSourceUseCase(ChunkSourceCommand.builder()
                        .sourceId(sourceId)
                        .build()))
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
    @PostMapping(path = "/chunk")
    public ResponseEntity<ResponseDto<?>> createChunk(
            @Valid
            @Parameter(name = "createChunkRequestDto", description = "청크 등록 정보", required = true)
            @RequestBody
            CreateChunkRequestDto createChunkRequestDto
    ) {

        chunkUseCase.createChunkUseCase(CreateChunkCommand.builder()
                .passageId(createChunkRequestDto.getPassageId())
                .content(createChunkRequestDto.getContent())
                .build());

        return ResponseEntity.ok(Response.CREATE_CHUNK_SUCCESS.toResponseDto());
    }

    @Operation(summary = "청크 조회 (청크 ID 기준)")
    @GetMapping(path = "/chunk/{chunkId}")
    public ResponseEntity<ResponseDto<GetChunkResponseDto>> getChunk(@PathVariable("chunkId") Long chunkId) {

        ChunkVo chunkVo = chunkUseCase.getChunkUseCase(GetChunkCommand.builder()
                .chunkId(chunkId)
                .build());

        GetChunkResponseDto getChunkResponseDto = GetChunkResponseDto.of(chunkVo);

        return ResponseEntity.ok(Response.GET_CHUNK_SUCCESS.toResponseDto(getChunkResponseDto));
    }

    @Operation(summary = "청크 목록 조회 (패시지 ID 기준, 페이징)")
    @GetMapping(path = "/chunk")
    public ResponseEntity<ResponseDto<PageResponseDto<GetChunkResponseDto>>> getChunks(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {

        PageWrapper<ChunkVo> chunkVoPageWrapper = chunkUseCase.getChunksUseCase(GetChunksCommand.builder()
                .page(page)
                .size(size)
                .build());

        PageResponseDto<GetChunkResponseDto> pageResponseDto = PageResponseDto.<GetChunkResponseDto>builder()
                .content(GetChunkResponseDto.toList(chunkVoPageWrapper.getData()))
                .isLast(chunkVoPageWrapper.isLast())
                .pageNo(chunkVoPageWrapper.getPage())
                .pageSize(chunkVoPageWrapper.getSize())
                .totalCount(chunkVoPageWrapper.getTotalCount())
                .totalPages(chunkVoPageWrapper.getTotalPages())
                .build();

        return ResponseEntity.ok(Response.GET_CHUNKS_SUCCESS.toResponseDto(pageResponseDto));
    }

    @Operation(summary = "청크 수정")
    @PutMapping(path = "/chunk/{chunkId}")
    public ResponseEntity<ResponseDto<?>> updateChunk(
            @Parameter(name = "chunkId", description = "청크 ID", required = true)
            @PathVariable("chunkId")
            Long chunkId,
            @Valid
            @Parameter(name = "updateChunkRequestDto", description = "청크 수정 정보", required = true)
            @RequestBody
            UpdateChunkRequestDto updateChunkRequestDto
    ) {

        chunkUseCase.updateChunkUseCase(UpdateChunkCommand.builder()
                .chunkId(chunkId)
                .content(updateChunkRequestDto.getContent())
                .build());

        return ResponseEntity.ok(Response.UPDATE_CHUNK_SUCCESS.toResponseDto());
    }

    @Operation(summary = "청크 삭제")
    @DeleteMapping(path = "/chunk/{chunkId}")
    public ResponseEntity<ResponseDto<?>> deleteChunk(@PathVariable("chunkId") Long chunkId) {

        chunkUseCase.deleteChunkUseCase(DeleteChunkCommand.builder()
                .chunkId(chunkId)
                .build());

        return ResponseEntity.ok(Response.DELETE_CHUNK_SUCCESS.toResponseDto());
    }

    @Operation(summary = "패시지 조회 (패시지 ID 기준)")
    @GetMapping(path = "/passage/{passageId}")
    public ResponseEntity<ResponseDto<GetPassageResponseDto>> getPassage(@PathVariable("passageId") Long passageId) {

        PassageVo passageVo = chunkUseCase.getPassageUseCase(GetPassageCommand.builder()
                .passageId(passageId)
                .build());

        GetPassageResponseDto getPassageResponseDto = GetPassageResponseDto.of(passageVo);

        return ResponseEntity.ok(Response.GET_PASSAGE_SUCCESS.toResponseDto(getPassageResponseDto));
    }

    @Operation(summary = "패시지 목록 조회 (대상 문서 ID 기준, 페이징)")
    @GetMapping(path = "/passage")
    public ResponseEntity<ResponseDto<PageResponseDto<GetPassageResponseDto>>> getPassages(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {

        PageWrapper<PassageVo> passageVoPageWrapper = chunkUseCase.getPassagesUseCase(GetPassagesCommand.builder()
                .page(page)
                .size(size)
                .build());

        PageResponseDto<GetPassageResponseDto> pageResponseDto = PageResponseDto.<GetPassageResponseDto>builder()
                .content(GetPassageResponseDto.toList(passageVoPageWrapper.getData()))
                .isLast(passageVoPageWrapper.isLast())
                .pageNo(passageVoPageWrapper.getPage())
                .pageSize(passageVoPageWrapper.getSize())
                .totalCount(passageVoPageWrapper.getTotalCount())
                .totalPages(passageVoPageWrapper.getTotalPages())
                .build();

        return ResponseEntity.ok(Response.GET_PASSAGES_SUCCESS.toResponseDto(pageResponseDto));
    }
}