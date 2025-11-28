package com.extractor.domain.model;

import com.extractor.domain.vo.PatternVo;
import com.extractor.global.enums.SelectType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class PassageOption {

    private final int depthSize;

    private final List<PatternVo> patterns;

    private final SelectType type;

    private final boolean isExtractTitle;

    @Builder
    public PassageOption(List<PatternVo> patterns, SelectType type, boolean isExtractTitle) {
        this.patterns = patterns == null ? new ArrayList<>() : patterns;
        this.depthSize = this.patterns.size();
        this.type = type;
        this.isExtractTitle = isExtractTitle;
    }
}
