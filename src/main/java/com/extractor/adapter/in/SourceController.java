package com.extractor.adapter.in;

import com.extractor.adapter.in.dto.RepoResourceDto;
import com.extractor.adapter.in.request.CreateFileSourceRequestDto;
import com.extractor.adapter.in.request.CreateRepoSourceRequestDto;
import com.extractor.adapter.in.response.ResponseDto;
import com.extractor.adapter.propery.FileProperty;
import com.extractor.application.command.CreateSourceCommand;
import com.extractor.application.enums.SourceType;
import com.extractor.application.usecase.SourceUseCase;
import com.extractor.application.vo.FileVo;
import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
import com.extractor.global.utils.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Validated
@Tag(name = "SourceController", description = "대상 문서 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/source")
@RestController
public class SourceController {

    private final SourceUseCase sourceUseCase;
    private final FileProperty fileProperty;
    private final FileUtil fileUtil;

    @Operation(summary = "파일 대상 문서 등록")
    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<?>> createFileSources(
            @Parameter(name = "createFileSourceRequestDto", description = "파일 대상 문서 등록 정보", required = true)
            @RequestPart("requestDto")
            CreateFileSourceRequestDto createFileSourceRequestDto,
            @Parameter(name = "multipartFile", description = "업로드 파일 목록", required = true)
            @RequestPart("uploadFile")
            List<MultipartFile> multipartFiles
    ) {
        for (MultipartFile multipartFile : multipartFiles) {
            FileVo fileVo = null;

            try {
                fileVo = fileUtil.uploadFile(multipartFile, fileProperty.getTempDir());

                sourceUseCase.createSourcesUseCase(CreateSourceCommand.builder()
                        .sourceType(SourceType.FILE.getCode())
                        .categoryCode(createFileSourceRequestDto.getCategoryCode())
                        .collectionId(createFileSourceRequestDto.getCollectionId())
                        .maxTokenSize(createFileSourceRequestDto.getMaxTokenSize())
                        .overlapSize(createFileSourceRequestDto.getOverlapSize())
                        .patterns(createFileSourceRequestDto.getPatterns().stream()
                                .map(patternDto -> PatternVo.builder()
                                        .build())
                                .toList())
                        .stopPatterns(createFileSourceRequestDto.getStopPatterns())
                        .file(fileVo)
                        .build());

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패");
            } finally {
                if (fileVo != null) {
                    fileUtil.deleteFile(Paths.get(fileVo.getUrl()));
                }
            }
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .message("대상 문서 등록 성공")
                .data(Collections.emptyMap())
                .build());
    }

    @Operation(summary = "원격 대상 문서 등록")
    @PostMapping(path = "/repo")
    public ResponseEntity<ResponseDto<?>> createRepoSources(
            @Parameter(name = "createRemoteSourceRequestDto", description = "원격 대상 문서 등록 정보", required = true)
            @RequestBody
            CreateRepoSourceRequestDto createRepoSourceRequestDto
    ) {
        String ip = createRepoSourceRequestDto.getHost() + ":" +  createRepoSourceRequestDto.getPort();

        for (RepoResourceDto repoResourceDto : createRepoSourceRequestDto.getRemoteResources()) {
            FileVo fileVo = FileVo.builder()
                    .originFileName(repoResourceDto.getOriginFileName())
                    .fileName(repoResourceDto.getFileName())
                    .ip(ip)
                    .filePath(createRepoSourceRequestDto.getPath())
                    .fileSize(0)
                    .ext(repoResourceDto.getExt())
                    .url(repoResourceDto.getUrl())
                    .build();

            sourceUseCase.createSourcesUseCase(CreateSourceCommand.builder()
                    .sourceType(SourceType.REPO.getCode())
                    .categoryCode(createRepoSourceRequestDto.getCategoryCode())
                    .collectionId(createRepoSourceRequestDto.getCollectionId())
                    .maxTokenSize(createRepoSourceRequestDto.getMaxTokenSize())
                    .overlapSize(createRepoSourceRequestDto.getOverlapSize())
                    .patterns(createRepoSourceRequestDto.getPatterns().stream()
                            .map(patternDto -> PatternVo.builder()
                                    .tokenSize(patternDto.getTokenSize())
                                    .prefixes(patternDto.getPrefixes().stream()
                                            .map(prefixDto -> PrefixVo.builder()
                                                    .prefix(prefixDto.getPrefix())
                                                    .isTitle(prefixDto.getIsTitle())
                                                    .build())
                                            .toList())
                                    .build())
                            .toList())
                    .stopPatterns(createRepoSourceRequestDto.getStopPatterns())
                    .file(fileVo)
                    .build());
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .message("대상 문서 등록 성공")
                .data(Collections.emptyMap())
                .build());
    }
}
