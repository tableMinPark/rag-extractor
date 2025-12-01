package com.document.extractor.domain.vo;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.domain.model.SourcePattern;
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

    private final SelectType selectType;

    @Builder
    public PassageOptionVo(List<SourcePattern> patterns, SelectType selectType) {
        this.patterns = patterns == null ? new ArrayList<>() : patterns;
        this.depthSize = this.patterns.size();
        this.selectType = selectType;
    }
}
