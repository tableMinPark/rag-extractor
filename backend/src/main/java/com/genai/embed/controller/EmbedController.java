package com.genai.embed.controller;

import com.genai.extractor.adapter.in.dto.response.ChunkBatchResponseDto;
import com.genai.embed.service.EmbedService;
import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/embed")
public class EmbedController {

    private final EmbedService embedService;

    @PostMapping("/{sourceId}")
    public ResponseEntity<ResponseDto<ChunkBatchResponseDto>> embedChunk(@PathVariable Long sourceId) {
        ChunkBatchResponseDto result = embedService.embedSource(sourceId);
        return ResponseEntity.ok(Response.EMBED_SUCCESS.toResponseDto(result));
    }

    @DeleteMapping("/{sourceId}")
    public ResponseEntity<ResponseDto<Void>> deleteEmbed(@PathVariable Long sourceId) {
        embedService.deleteEmbedSource(sourceId);
        return ResponseEntity.ok(Response.DELETE_EMBED_SUCCESS.toResponseDto(null));
    }
}
