package com.extractor.adapter.out;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LawLinkInfoVo {

    private final Long lawContentId;

    private final Integer version;

    private final Long lawId;

    private final String linkCode;
}
