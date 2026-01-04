package com.document.extractor.application.command;

import com.document.extractor.domain.vo.PatternVo;
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

    private final String uri;
}
