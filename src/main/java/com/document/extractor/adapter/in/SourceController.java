package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.etc.RepoResourceDto;
import com.document.extractor.adapter.in.dto.request.CreateFileSourceRequestDto;
import com.document.extractor.adapter.in.dto.request.CreateRepoSourceRequestDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.application.command.CreateSourceCommand;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.application.usecase.SourceUseCase;
import com.document.extractor.application.utils.FileUtil;
import com.document.extractor.application.vo.FileVo;
import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.domain.vo.PrefixVo;
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
import java.util.Collections;
import java.util.List;

@Validated
@Tag(name = "SourceController", description = "대상 문서 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/source")
@RestController
public class SourceController {

    private final SourceUseCase sourceUseCase;
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
                fileVo = fileUtil.uploadFile(multipartFile);

                sourceUseCase.createSourcesUseCase(CreateSourceCommand.builder()
                        .sourceType(SourceType.FILE.name())
                        .categoryCode(createFileSourceRequestDto.getCategoryCode())
                        .collectionId(createFileSourceRequestDto.getCollectionId())
                        .maxTokenSize(createFileSourceRequestDto.getMaxTokenSize())
                        .overlapSize(createFileSourceRequestDto.getOverlapSize())
                        .patterns(createFileSourceRequestDto.getPatterns().stream()
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
                        .stopPatterns(createFileSourceRequestDto.getStopPatterns())
                        .selectType(createFileSourceRequestDto.getSelectType().toUpperCase())
                        .file(fileVo)
                        .build());

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패");
            } catch (RuntimeException e) {
                if (fileVo != null && fileVo.getUrl() != null) {
                    fileUtil.deleteFile(Paths.get(fileVo.getUrl()));
                }
                throw e;
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
            @Valid
            @Parameter(name = "createRemoteSourceRequestDto", description = "원격 대상 문서 등록 정보", required = true)
            @RequestBody
            CreateRepoSourceRequestDto createRepoSourceRequestDto
    ) {
        String ip = createRepoSourceRequestDto.getHost() + ":" +  createRepoSourceRequestDto.getPort();

        for (RepoResourceDto repoResourceDto : createRepoSourceRequestDto.getRepoResources()) {

            String filePath = String.format("http://%s:%d/%s",
                    createRepoSourceRequestDto.getHost(),
                    createRepoSourceRequestDto.getPort(),
                    repoResourceDto.getPath());

            String url = String.format("http://%s:%d/%s/%s",
                    createRepoSourceRequestDto.getHost(),
                    createRepoSourceRequestDto.getPort(),
                    repoResourceDto.getPath(),
                    repoResourceDto.getUrn());

            FileVo fileVo = FileVo.builder()
                    .originFileName(repoResourceDto.getOriginFileName())
                    .fileName(repoResourceDto.getFileName())
                    .ip(ip)
                    .filePath(filePath)
                    .fileSize(0)
                    .ext(repoResourceDto.getExt())
                    .url(url)
                    .build();

            sourceUseCase.createSourcesUseCase(CreateSourceCommand.builder()
                    .sourceType(SourceType.REPO.name())
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
                    .selectType(createRepoSourceRequestDto.getSelectType().toUpperCase())
                    .file(fileVo)
                    .build());
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .message("대상 문서 등록 성공")
                .data(Collections.emptyMap())
                .build());
    }
}
