package com.extractor.domain.model;

import com.extractor.global.enums.FileExtension;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ToString
@Getter
public class ExtractDocument {

    private final String docId;

    private final String name;

    private final FileExtension extension;

    private final Path path;

    private final List<DocumentLine> lines;

    public ExtractDocument(String docId, String name, FileExtension extension, Path path) {
        this.docId = docId;
        this.name = name;
        this.extension = extension;
        this.path = path;
        this.lines = new ArrayList<>();
    }

    /**
     * 문자 데이터 등록
     */
    public void addText(String text) {
        String content = text.trim();

        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TEXT)
                    .content(content)
                    .build());
        }
    }

    /**
     * 표 데이터 등록
     */
    public void addTable(String table) {
        String content = table.trim();

        if (!table.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TABLE)
                    .content(content)
                    .build());
        }
    }

    /**
     * 이미지 데이터 등록
     */
    public void addImage(String text) {
        String content = text.trim();

        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.IMAGE)
                    .content(content)
                    .build());
        }
    }
}
