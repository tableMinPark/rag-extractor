package com.extractor.domain.vo.hwpx;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@ToString
@Getter
public class HwpxImageVo {

    private final String id;

    private final Path path;

    private final String extension;

    @Builder
    public HwpxImageVo(String id, Path path, String extension) {
        this.id = id;
        this.path = path;
        this.extension = extension;
    }
}
