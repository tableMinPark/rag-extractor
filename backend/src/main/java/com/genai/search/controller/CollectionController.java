package com.genai.search.controller;

import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import com.genai.search.controller.dto.response.CollectionResponseDto;
import com.genai.search.type.CollectionTypeFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CollectionController", description = "컬렉션 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/collection")
public class CollectionController {

    private final CollectionTypeFactory collectionTypeFactory;

    @Operation(summary = "컬렉션 목록 조회")
    @GetMapping
    public ResponseEntity<ResponseDto<List<CollectionResponseDto>>> getCollections() {
        List<CollectionResponseDto> result = CollectionResponseDto.fromList(collectionTypeFactory.getAll());
        return ResponseEntity.ok(Response.GET_COLLECTIONS_SUCCESS.toResponseDto(result));
    }
}
