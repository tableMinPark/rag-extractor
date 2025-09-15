package com.extractor.extract.domain.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@ToString
@Getter
public class HwpxImageVo {

    private final String id;

    private final String content;

    private final Path path;

    private final String extension;

    @Builder
    public HwpxImageVo(String id, String content, Path path, String extension) {
        this.id = id;
        this.content = content;
        this.path = path;
        this.extension = extension;
    }
}