package com.genai.search.controller;

import com.genai.common.service.CommonCodeService;
import com.genai.common.vo.CommonCodeVO;
import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import com.genai.search.dto.request.SearchRequestDto;
import com.genai.search.dto.response.CategoryResponseDto;
import com.genai.search.dto.response.SearchResponseDto;
import com.genai.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SearchController", description = "RAG 검색 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final CommonCodeService commonCodeService;

    private static final String CATEGORY_CODE_GROUP = "TRAIN";

    @Operation(summary = "검색 카테고리 목록 조회")
    @GetMapping("/category")
    public ResponseEntity<ResponseDto<List<CategoryResponseDto>>> getCategories() {
        List<CommonCodeVO> vos = commonCodeService.getCommonCodes(CATEGORY_CODE_GROUP);
        List<CategoryResponseDto> result = CategoryResponseDto.fromList(vos);
        return ResponseEntity.ok(Response.GET_SEARCH_CATEGORIES_SUCCESS.toResponseDto(result));
    }

    @Operation(summary = "키워드 검색")
    @PostMapping("/keyword")
    public ResponseEntity<ResponseDto<List<SearchResponseDto>>> keywordSearch(
            @Valid @RequestBody SearchRequestDto request) {
        List<SearchResponseDto> result = searchService.keywordSearch(request);
        return ResponseEntity.ok(Response.KEYWORD_SEARCH_SUCCESS.toResponseDto(result));
    }

    @Operation(summary = "벡터 검색")
    @PostMapping("/vector")
    public ResponseEntity<ResponseDto<List<SearchResponseDto>>> vectorSearch(
            @Valid @RequestBody SearchRequestDto request) {
        List<SearchResponseDto> result = searchService.vectorSearch(request);
        return ResponseEntity.ok(Response.VECTOR_SEARCH_SUCCESS.toResponseDto(result));
    }
}
