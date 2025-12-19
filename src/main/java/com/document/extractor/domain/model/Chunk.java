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

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private String compactContent;

    private Integer tokenSize;

    private Integer compactTokenSize;

    private final LocalDateTime sysCreateDt;

    private final LocalDateTime sysModifyDt;

    public void update(Long passageId, Long version) {
        this.passageId = passageId;
        this.version = version;
    }

    public void update(String title, String subTitle, String thirdTitle,
                       String content, String subContent, String compactContent,
                       int tokenSize, int compactTokenSize) {
        this.title = title;
        this.subTitle = subTitle;
        this.thirdTitle = thirdTitle;
        this.content = content;
        this.subContent = subContent;
        this.compactContent = compactContent;
        this.tokenSize = tokenSize;
        this.compactTokenSize = compactTokenSize;
    }
}
