package com.document.extractor.domain.vo;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.domain.model.SourcePattern;
import com.document.extractor.domain.model.SourceStopPattern;
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
