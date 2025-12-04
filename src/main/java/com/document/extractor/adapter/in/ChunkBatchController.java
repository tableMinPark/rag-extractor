package com.document.extractor.adapter.in;

import com.document.extractor.adapter.in.dto.response.ChunkResponseDto;
import com.document.extractor.adapter.in.dto.response.ResponseDto;
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
@Tag(name = "ChunkBatchController", description = "청킹 배치 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/chunk/batch")
@RestController
public class ChunkBatchController {

    private final SourceUseCase sourceUseCase;
    private final ChunkUseCase chunkUseCase;

    @Operation(summary = "청킹 배치")
    @PostMapping("/{sourceId}")
    public ResponseEntity<ResponseDto<ChunkResponseDto>> chunkBatch(
            @Parameter(name = "sourceId", description = "대상 문서 ID", required = true)
            @PathVariable(value = "sourceId")
            Long sourceId
    ) {
        ChunkResultVo chunkResultVo = chunkUseCase.chunkBatchUseCase(ChunkBatchCommand.builder()
                .sourceId(sourceId)
                .build());

        ChunkResponseDto chunkResponseDto = ChunkResponseDto.builder()
                .source(chunkResultVo.getSource())
                .passages(chunkResultVo.getPassages())
                .chunks(chunkResultVo.getChunks())
                .build();

        return ResponseEntity.ok(ResponseDto.<ChunkResponseDto>builder()
                .message("청킹 배치 성공")
                .data(chunkResponseDto)
                .build());
    }

    @Operation(summary = "청킹 일괄 배치")
    @PostMapping
    public ResponseEntity<ResponseDto<List<ChunkResponseDto>>> chunkBatches() {

        List<ChunkResponseDto> chunkResponseDtos = new ArrayList<>();

        sourceUseCase.getActiveSourcesUseCase().forEach(sourceVo -> {
            ChunkResultVo chunkResultVo = chunkUseCase.chunkBatchUseCase(ChunkBatchCommand.builder()
                    .sourceId(sourceVo.getSourceId())
                    .build());

            chunkResponseDtos.add(ChunkResponseDto.builder()
                    .source(chunkResultVo.getSource())
                    .passages(chunkResultVo.getPassages())
                    .chunks(chunkResultVo.getChunks())
                    .build());

            log.info("[{}] {} 전처리 완료", chunkResultVo.getSource().getSourceId(), chunkResultVo.getSource().getName());
        });

        return ResponseEntity.ok(ResponseDto.<List<ChunkResponseDto>>builder()
                .message("청킹 다중 배치 성공")
                .data(chunkResponseDtos)
                .build());
    }
}
