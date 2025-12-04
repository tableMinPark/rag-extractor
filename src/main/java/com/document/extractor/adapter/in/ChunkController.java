package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import com.document.extractor.adapter.in.dto.request.ChunkReposRequestDto;
import com.document.extractor.adapter.in.dto.request.ChunkRequestDto;
import com.document.extractor.adapter.in.dto.response.ChunkResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.vo.ChunkResultVo;
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
@RequestMapping("/chunk")
@RestController
public class ChunkController {

    private final ChunkUseCase chunkUseCase;
    private final FileProperty fileProperty;

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
        List<ChunkResultVo> chunkResultVos = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            UploadFile uploadFile = FileUtil.uploadFile(multipartFile, fileProperty.getFileStorePath(), fileProperty.getTempDir());

            try {
                chunkResultVos.add(chunkUseCase.chunkFileUseCase(ChunkFileCommand.builder()
                        .extractType(chunkRequestDto.getExtractType())
                        .selectType(chunkRequestDto.getSelectType())
                        .patterns(PatternDto.toPatternVo(chunkRequestDto.getPatterns()))
                        .stopPatterns(chunkRequestDto.getStopPatterns())
                        .maxTokenSize(chunkRequestDto.getMaxTokenSize())
                        .overlapSize(chunkRequestDto.getOverlapSize())
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
                        .source(chunkResultVo.getSource())
                        .passages(chunkResultVo.getPassages())
                        .chunks(chunkResultVo.getChunks())
                        .build())
                .toList();

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("파일 청킹 성공")
                .data(chunkResponseDtos)
                .build());
    }

    @Operation(summary = "원격 문서 청킹")
    @PostMapping(path = "/repo")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkRepos(
            @Valid
            @Parameter(name = "chunkReposRequestDto", description = "원격 문서 청킹 정보", required = true)
            @RequestBody
            ChunkReposRequestDto chunkReposRequestDto
    ) {
        List<ChunkResultVo> chunkResultVos = chunkReposRequestDto.getRepoIds().stream()
                .map(repoId -> chunkUseCase.chunkRepoUseCase(ChunkRepoCommand.builder()
                        .extractType(chunkReposRequestDto.getExtractType())
                        .selectType(chunkReposRequestDto.getSelectType())
                        .patterns(PatternDto.toPatternVo(chunkReposRequestDto.getPatterns()))
                        .stopPatterns(chunkReposRequestDto.getStopPatterns())
                        .maxTokenSize(chunkReposRequestDto.getMaxTokenSize())
                        .overlapSize(chunkReposRequestDto.getOverlapSize())
                        .repoType(chunkReposRequestDto.getRepoType())
                        .repoId(repoId)
                        .build()))
                .toList();

        List<ChunkResponseDto> chunkResponseDtos = chunkResultVos.stream()
                .map(chunkResultVo -> ChunkResponseDto.builder()
                        .source(chunkResultVo.getSource())
                        .passages(chunkResultVo.getPassages())
                        .chunks(chunkResultVo.getChunks())
                        .build())
                .toList();

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("원격 청킹 성공")
                .data(chunkResponseDtos)
                .build());
    }
}