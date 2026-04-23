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
public class CreateSourceCommand {

    private String sourceType;

    private String categoryCode;

    private String collectionId;

    private Integer maxTokenSize;

    private Integer overlapSize;

    private List<PatternVo> patterns;

    private List<String> stopPatterns;

    private String selectType;

    private UploadFile file;

    private Boolean isAuto;
}
