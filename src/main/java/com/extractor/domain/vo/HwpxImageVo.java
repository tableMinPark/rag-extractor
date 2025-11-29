package com.extractor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class HwpxImageVo {

    private final String id;

    private final String content;

    private final Path path;

    private final String ext;
}