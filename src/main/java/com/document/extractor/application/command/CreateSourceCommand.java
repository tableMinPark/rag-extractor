package com.document.extractor.application.command;

import com.document.extractor.application.vo.FileVo;
import com.document.extractor.domain.vo.PatternVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class CreateSourceCommand {

    private String sourceType;

    private String categoryCode;

    private String collectionId;

    private Integer maxTokenSize;

    private Integer overlapSize;

    private List<PatternVo> patterns;

    private List<String> stopPatterns;

    private FileVo file;
}
