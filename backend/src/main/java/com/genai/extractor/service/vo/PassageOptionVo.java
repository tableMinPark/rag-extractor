package com.genai.extractor.service.vo;

import com.genai.extractor.enums.SelectType;
import com.genai.extractor.service.domain.model.SourcePattern;
import com.genai.extractor.service.domain.model.SourceStopPattern;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class PassageOptionVo {

    private final int depthSize;

    private final List<SourcePattern> patterns;

    private final List<SourceStopPattern> stopPatterns;

    private final SelectType selectType;

    @Builder
    public PassageOptionVo(List<SourcePattern> patterns, List<SourceStopPattern> stopPatterns, SelectType selectType) {
        this.patterns = patterns == null ? new ArrayList<>() : patterns;
        this.stopPatterns = stopPatterns == null ? new ArrayList<>() : stopPatterns;
        this.depthSize = this.patterns.size();
        this.selectType = selectType;
    }
}
