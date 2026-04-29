package com.genai.extractor.service.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PdfSectionVo {

    private final int page;

    private final String text;

    private final List<List<List<String>>> tables;
}
