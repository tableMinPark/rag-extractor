package com.genai.extractor.controller;

import com.genai.extractor.controller.dto.response.GetPassageResponseDto;
import com.genai.extractor.vo.PassageVo;
import com.genai.extractor.service.PassageService;
import com.genai.global.wrapper.PageWrapper;
import com.genai.global.dto.PageResponseDto;
import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
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

    private final PassageService passageService;

    @Operation(summary = "패시지 조회 (패시지 ID 기준)")
    @GetMapping(path = "/{passageId}")
    public ResponseEntity<ResponseDto<GetPassageResponseDto>> getPassage(@PathVariable("passageId") Long passageId) {
        PassageVo passageVo = passageService.getPassageVo(passageId);
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
        PageWrapper<PassageVo> passageVoPageWrapper = passageService.getPassageVos(page, size, sourceId);

        PageResponseDto<GetPassageResponseDto> pageResponseDto = PageResponseDto.<GetPassageResponseDto>builder()
                .content(GetPassageResponseDto.toList(passageVoPageWrapper.getContent()))
                .isLast(passageVoPageWrapper.isLast())
                .pageNo(passageVoPageWrapper.getPageNo())
                .pageSize(passageVoPageWrapper.getPageSize())
                .totalCount(passageVoPageWrapper.getTotalCount())
                .totalPages(passageVoPageWrapper.getTotalPages())
                .build();

        return ResponseEntity.ok(Response.GET_PASSAGES_SUCCESS.toResponseDto(pageResponseDto));
    }
}
