package com.genai.embed.controller;

import com.document.extractor.adapter.in.dto.response.ChunkBatchResponseDto;
import com.genai.embed.service.EmbedService;
import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/embed")
public class EmbedController {

    private final EmbedService embedService;

    @PostMapping("/{sourceId}")
    public ResponseEntity<ResponseDto<ChunkBatchResponseDto>> embedChunk(@PathVariable Long sourceId) {

        ChunkBatchResponseDto chunkBatchResponseDto = null;

        return ResponseEntity.ok(Response.BATCH_CHUNK_SUCCESS.toResponseDto(chunkBatchResponseDto));
    }
}
