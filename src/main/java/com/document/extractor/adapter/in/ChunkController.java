package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.etc.PatternDto;
import com.document.extractor.adapter.in.dto.request.ChunkReposRequestDto;
import com.document.extractor.adapter.in.dto.request.ChunkRequestDto;
import com.document.extractor.adapter.in.dto.response.ChunkResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.ChunkFileCommand;
import com.document.extractor.application.command.ChunkRepoCommand;
import com.document.extractor.application.enums.SelectType;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.vo.*;
import com.document.global.enums.ExtractType;
import com.document.extractor.application.utils.FileUtil;
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

    @Operation(summary = "원격 문서 청킹")
    @PostMapping(path = "/repo")
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkRepos(
            @Valid
            @Parameter(name = "chunkLawsRequestDto", description = "원격 문서 청킹 정보", required = true)
            @RequestBody
            ChunkReposRequestDto chunkReposRequestDto
    ) {
        ChunkOptionVo chunkOptionVo = ChunkOptionVo.builder()
                .extractType(ExtractType.find(chunkReposRequestDto.getExtractType()))
                .selectType(SelectType.REGEX)
                .patterns(PatternDto.toPatternVo(chunkReposRequestDto.getPatterns()))
                .antiPatterns(chunkReposRequestDto.getExcludeContentTypes())
                .maxTokenSize(chunkReposRequestDto.getMaxTokenSize())
                .overlapSize(chunkReposRequestDto.getOverlapSize())
                .build();

        List<SourceVo> sourceVos = chunkReposRequestDto.getRepoIds().stream()
                .map(lawId -> chunkUseCase.chunkRepoUseCase(ChunkRepoCommand.builder()
                        .repoType(chunkReposRequestDto.getRepoType())
                        .chunkOption(chunkOptionVo)
                        .repoId(lawId)
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
                .message("원격 청킹 성공")
                .data(chunkResponseDtos)
                .build());
    }
}