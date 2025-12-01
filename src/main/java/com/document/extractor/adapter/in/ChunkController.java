package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import com.document.extractor.adapter.in.dto.request.ChunkReposRequestDto;
import com.document.extractor.adapter.in.dto.request.ChunkRequestDto;
import com.document.extractor.adapter.in.dto.response.ChunkResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.ChunkBatchCommand;
import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.utils.FileUtil;
import com.document.extractor.application.vo.ChunkResultVo;
import com.document.extractor.application.vo.FileVo;
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

import java.io.IOException;
import java.nio.file.Paths;
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
    private final FileUtil fileUtil;

    @Operation(summary = "청킹 배치")
    @PostMapping("/batch/{sourceId}")
    public ResponseEntity<ResponseDto<ChunkResponseDto>> chunkBatch(
            @Parameter(name = "sourceId", description = "대상 문서 ID", required = true)
            @PathVariable(value = "sourceId")
            Long sourceId
    ) {
        ChunkResultVo chunkResultVo = chunkUseCase.chunkBatchUseCase(ChunkBatchCommand.builder()
                .sourceId(sourceId)
                .build());

        ChunkResponseDto chunkResponseDto = ChunkResponseDto.builder()
                .source(chunkResultVo.getSource())
                .passages(chunkResultVo.getPassages())
                .chunks(chunkResultVo.getChunks())
                .build();

        return ResponseEntity.ok(ResponseDto.<ChunkResponseDto>builder()
                .message("청킹 배치 성공")
                .data(chunkResponseDto)
                .build());
    }

    @Operation(summary = "청킹 일괄 배치")
    @PostMapping("/batch")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkBatches() {

        List<ChunkResponseDto> chunkResponseDtos = new ArrayList<>();

        chunkUseCase.getActiveSourcesUseCase().forEach(sourceVo -> {
            ChunkResultVo chunkResultVo = chunkUseCase.chunkBatchUseCase(ChunkBatchCommand.builder()
                    .sourceId(sourceVo.getSourceId())
                    .build());

            chunkResponseDtos.add(ChunkResponseDto.builder()
                    .source(chunkResultVo.getSource())
                    .passages(chunkResultVo.getPassages())
                    .chunks(chunkResultVo.getChunks())
                    .build());

            log.info("[{}] {} 전처리 완료", chunkResultVo.getSource().getSourceId(), chunkResultVo.getSource().getName());
        });

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("청킹 다중 배치 성공")
                .data(chunkResponseDtos)
                .build());
    }

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
            FileVo fileVo = null;

            try {
                fileVo = fileUtil.uploadFile(multipartFile, fileProperty.getTempDir());
                chunkResultVos.add(chunkUseCase.chunkFileUseCase(ChunkFileCommand.builder()
                        .extractType(chunkRequestDto.getExtractType())
                        .selectType(chunkRequestDto.getSelectType())
                        .patterns(PatternDto.toPatternVo(chunkRequestDto.getPatterns()))
                        .stopPatterns(chunkRequestDto.getStopPatterns())
                        .maxTokenSize(chunkRequestDto.getMaxTokenSize())
                        .overlapSize(chunkRequestDto.getOverlapSize())
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