package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.etc.RepoResourceDto;
import com.document.extractor.adapter.in.dto.request.CreateFileSourceRequestDto;
import com.document.extractor.adapter.in.dto.request.CreateRepoSourceRequestDto;
import com.document.extractor.adapter.in.dto.response.GetSourceResponseDto;
import com.document.extractor.adapter.in.dto.response.PageResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.in.enums.Response;
import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.command.CreateSourceCommand;
import com.document.extractor.application.command.GetSourceCommand;
import com.document.extractor.application.command.GetSourcesCommand;
import com.document.extractor.application.enums.SourceType;
import com.document.extractor.application.usecase.SourceUseCase;
import com.document.extractor.application.vo.SourceVo;
import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.vo.PatternVo;
import com.document.extractor.domain.vo.PrefixVo;
import com.document.global.utils.FileUtil;
import com.document.global.vo.UploadFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@Tag(name = "SourceController", description = "대상 문서 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/source")
@RestController
public class SourceController {

    private final SourceUseCase sourceUseCase;
    private final FileProperty fileProperty;

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
            UploadFile uploadFile = FileUtil.uploadFile(multipartFile, fileProperty.getFileStorePath());

            try {
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
                        .file(uploadFile)
                        .isAuto(createFileSourceRequestDto.getIsAuto())
                        .build());

            } catch (RuntimeException e) {
                if (uploadFile != null && uploadFile.getUrl() != null) {
                    FileUtil.deleteFile(uploadFile.getUrl());
                }
                throw e;
            }
        }

        return ResponseEntity.ok(Response.CREATE_FILE_SOURCE_SUCCESS.toResponseDto());
    }

    @Operation(summary = "원격 대상 문서 등록")
    @PostMapping(path = "/repo")
    public ResponseEntity<ResponseDto<?>> createRepoSources(
            @Valid
            @Parameter(name = "createRepoSourceRequestDto", description = "원격 대상 문서 등록 정보", required = true)
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

            UploadFile uploadFile = UploadFile.builder()
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
                    .file(uploadFile)
                    .isAuto(createRepoSourceRequestDto.getIsAuto())
                    .build());
        }

        return ResponseEntity.ok(Response.CREATE_REPO_SOURCE_SUCCESS.toResponseDto());
    }

    @Operation(summary = "대상 문서 조회 (대상 문서 ID 기준)")
    @GetMapping("/{sourceId}")
    public ResponseEntity<ResponseDto<GetSourceResponseDto>> getSource(@PathVariable("sourceId") Long sourceId) {

        GetSourceResponseDto getSourceResponseDto = GetSourceResponseDto.of(sourceUseCase.getSourceUseCase(GetSourceCommand.builder()
                .sourceId(sourceId)
                .build()));

        return ResponseEntity.ok(Response.GET_SOURCE_SUCCESS.toResponseDto(getSourceResponseDto));
    }

    @Operation(summary = "대상 문서 목록 조회 (페이징)")
    @GetMapping
    public ResponseEntity<ResponseDto<PageResponseDto<GetSourceResponseDto>>> getSources(
            @Min(1)
            @RequestParam("page")
            int page,
            @Min(1) @Max(100)
            @RequestParam("size")
            Integer size,
            @RequestParam(value = "isAuto", required = false)
            Boolean isAuto,
            @RequestParam(value = "orderBy", defaultValue = "sourceId", required = false)
            String orderBy,
            @Pattern(regexp = "asc|desc", message = "asc | desc 만 지원")
            @RequestParam(value = "order", defaultValue = "desc", required = false)
            String order,
            @RequestParam(value = "keyword", defaultValue = "", required = false)
            String keyword
    ) {
        PageWrapper<SourceVo> sourceVoPageWrapper = sourceUseCase.getSourcesUseCase(GetSourcesCommand.builder()
                .page(page)
                .size(size)
                .isAuto(isAuto)
                .orderBy(orderBy)
                .order(order)
                .keyword(keyword)
                .build());

        PageResponseDto<GetSourceResponseDto> pageResponseDto = PageResponseDto.<GetSourceResponseDto>builder()
                .content(GetSourceResponseDto.toList(sourceVoPageWrapper.getData()))
                .isLast(sourceVoPageWrapper.isLast())
                .pageNo(sourceVoPageWrapper.getPage())
                .pageSize(sourceVoPageWrapper.getSize())
                .totalCount(sourceVoPageWrapper.getTotalCount())
                .totalPages(sourceVoPageWrapper.getTotalPages())
                .build();

        return ResponseEntity.ok(Response.GET_SOURCES_SUCCESS.toResponseDto(pageResponseDto));
    }
}
