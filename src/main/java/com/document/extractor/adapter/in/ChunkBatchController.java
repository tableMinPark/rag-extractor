package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.response.ChunkBatchResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
import com.document.extractor.adapter.in.enums.Response;
import com.document.extractor.application.command.ChunkBatchCommand;
import com.document.extractor.application.usecase.ChunkUseCase;
import com.document.extractor.application.usecase.SourceUseCase;
import com.document.extractor.application.vo.ChunkResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Validated
@Tag(name = "ChunkBatchController", description = "대상 문서 청킹 배치 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/chunk/batch")
@RestController
public class ChunkBatchController {

    private final SourceUseCase sourceUseCase;
    private final ChunkUseCase chunkUseCase;

    @Operation(summary = "대상 문서 청킹 배치")
    @PostMapping("/{sourceId}")
    public ResponseEntity<ResponseDto<ChunkBatchResponseDto>> chunkBatch(
            @Parameter(name = "sourceId", description = "대상 문서 ID", required = true)
            @PathVariable(value = "sourceId")
            Long sourceId
    ) {
        ChunkResultVo chunkResultVo = chunkUseCase.chunkBatchUseCase(ChunkBatchCommand.builder()
                .sourceId(sourceId)
                .build());

        ChunkBatchResponseDto chunkBatchResponseDto = ChunkBatchResponseDto.builder()
                .isConvertError(chunkResultVo.getIsConvertError())
                .fileName(chunkResultVo.getSource().getName())
                .version(chunkResultVo.getSource().getVersion())
                .totalPassageCount(chunkResultVo.getCurrentPassages().size())
                .totalChunkCount(chunkResultVo.getChunks().size())
                .build();

        log.info("[전처리 완료] {} : {}", chunkResultVo.getSource().getSourceId(), chunkResultVo.getSource().getName());

        return ResponseEntity.ok(Response.CHUNK_BATCH_SUCCESS.toResponseDto(chunkBatchResponseDto));
    }

    @Operation(summary = "대상 문서 청킹 일괄 배치")
    @PostMapping
    public ResponseEntity<ResponseDto<List<ChunkBatchResponseDto>>> chunkBatches() {

        List<ChunkBatchResponseDto> chunkBatchResponseDtos = new ArrayList<>();

        sourceUseCase.getActiveSourcesUseCase().forEach(sourceVo -> {
            ChunkResultVo chunkResultVo = chunkUseCase.chunkBatchUseCase(ChunkBatchCommand.builder()
                    .sourceId(sourceVo.getSourceId())
                    .build());

            chunkBatchResponseDtos.add(ChunkBatchResponseDto.builder()
                    .isConvertError(chunkResultVo.getIsConvertError())
                    .fileName(chunkResultVo.getSource().getName())
                    .version(chunkResultVo.getSource().getVersion())
                    .totalPassageCount(chunkResultVo.getCurrentPassages().size())
                    .totalChunkCount(chunkResultVo.getChunks().size())
                    .build());

            log.info("[다중 전처리 완료] {} : {}", chunkResultVo.getSource().getSourceId(), chunkResultVo.getSource().getName());
        });

        return ResponseEntity.ok(Response.CHUNK_BATCHES_SUCCESS.toResponseDto(chunkBatchResponseDtos));
    }
}
