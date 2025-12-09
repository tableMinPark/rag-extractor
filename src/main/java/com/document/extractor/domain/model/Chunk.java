package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class Chunk {

    private final Long chunkId;

    private Long passageId;

    private Long version;

    private final String title;

    private final String subTitle;

    private final String thirdTitle;

    private final String content;

    private final String subContent;

    private final String compactContent;

    private final Integer tokenSize;

    private final Integer compactTokenSize;

    private final LocalDateTime sysCreateDt;

    private final LocalDateTime sysModifyDt;

    public void update(Long passageId, Long version) {
        this.passageId = passageId;
        this.version = version;
    }
}
