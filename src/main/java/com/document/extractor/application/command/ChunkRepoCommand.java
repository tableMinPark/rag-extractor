package com.document.extractor.application.command;

import com.document.extractor.domain.vo.PatternVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ChunkRepoCommand {

    private final String extractType;

    private final String selectType;

    private final List<PatternVo> patterns;

    private final List<String> stopPatterns;

    private final int maxTokenSize;

    private final int overlapSize;

    private final String repoType;

    private final String repoId;
}
