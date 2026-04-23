package com.genai.extractor.application.command;

import com.genai.common.vo.UploadFile;
import com.genai.extractor.domain.vo.PatternVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ChunkFileCommand {

    private final String extractType;

    private final String selectType;

    private final List<PatternVo> patterns;

    private final List<String> stopPatterns;

    private final int maxTokenSize;

    private final int overlapSize;

    private final UploadFile file;
}
