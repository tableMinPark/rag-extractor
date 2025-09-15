package com.extractor.chunk.domain.vo;

import lombok.Getter;

@Getter
public class PrefixVo {

    private final String prefix;

    private final Boolean isDeleting;

    public PrefixVo(String prefix, Boolean isDeleting) {
        this.prefix = prefix;
        this.isDeleting = isDeleting;
    }
}