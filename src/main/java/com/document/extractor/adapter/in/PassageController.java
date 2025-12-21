package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.response.GetPassageResponseDto;
import com.document.extractor.adapter.in.dto.response.PageResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.in.enums.Response;
import com.document.extractor.application.command.GetPassageCommand;
import com.document.extractor.application.command.GetPassagesCommand;
import com.document.extractor.application.usecase.PassageUseCase;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.wrapper.PageWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@Tag(name = "PassageController", description = "패시지 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/passage")
public class PassageController {

    private final PassageUseCase passageUseCase;

    @Operation(summary = "패시지 조회 (패시지 ID 기준)")
    @GetMapping(path = "/{passageId}")
    public ResponseEntity<ResponseDto<GetPassageResponseDto>> getPassage(@PathVariable("passageId") Long passageId) {

        PassageVo passageVo = passageUseCase.getPassageUseCase(GetPassageCommand.builder()
                .passageId(passageId)
                .build());

        GetPassageResponseDto getPassageResponseDto = GetPassageResponseDto.of(passageVo);

        return ResponseEntity.ok(Response.GET_PASSAGE_SUCCESS.toResponseDto(getPassageResponseDto));
    }

    @Operation(summary = "패시지 목록 조회 (대상 문서 ID 기준, 페이징)")
    @GetMapping
    public ResponseEntity<ResponseDto<PageResponseDto<GetPassageResponseDto>>> getPassages(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sourceId") long sourceId
    ) {

        PageWrapper<PassageVo> passageVoPageWrapper = passageUseCase.getPassagesUseCase(GetPassagesCommand.builder()
                .sourceId(sourceId)
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