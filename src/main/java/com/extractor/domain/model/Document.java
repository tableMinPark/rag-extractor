package com.extractor.domain.model;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@ToString
@Getter
public class Document {

    private final String name;

    private final FileExtension extension;

    private final Path path;

    private final List<DocumentContent> documentContents;

    public Document(String name, FileExtension extension, Path path) {
        this(name, extension, path, new ArrayList<>());
    }


    public Document(String name, FileExtension extension, Path path, List<DocumentContent> documentContents) {
        this.name = name;
        this.extension = extension;
        this.path = path;
        this.documentContents = documentContents;
    }

    /**
     * 데이터 초기화
     */
    public void clearDocumentContents() {
        this.documentContents.clear();
    }

    /**
     * 전체 본문 조회
     * @return 전체 본문 문자열
     */
    public String getContent() {

        StringBuilder contentBuilder = new StringBuilder();

        this.documentContents.forEach(documentContent ->
                contentBuilder.append("\n")
                        .append(documentContent.getTitle())
                        .append("\n")
                        .append(documentContent.getContext()));

        return contentBuilder.toString().trim();
    }

    /**
     * 문자 데이터 등록
     */
    public void addTextContent(String text) {

        String context = text.trim();

        if (!text.trim().isBlank()) {
            this.documentContents.add(DocumentContent.builder()
                    .contentId(this.documentContents.size())
                    .compareText(context)
                    .context(context)
                    .subDocumentContents(Collections.emptyList())
                    .type(DocumentContent.LineType.TEXT)
                    .build());
        }
    }

    /**
     * 문자 데이터 등록
     */
    public void addTextContent(String title, String simpleTitle, String compareText, String context, List<DocumentContent> subDocumentContents) {

        this.documentContents.add(DocumentContent.builder()
                .contentId(this.documentContents.size())
                .title(title)
                .simpleTitle(simpleTitle)
                .compareText(compareText)
                .context(context)
                .subDocumentContents(subDocumentContents)
                .type(DocumentContent.LineType.TEXT)
                .build());
    }

    /**
     * 표 데이터 등록
     */
    public void addTableContent(String table) {

        String context = table.trim();

        if (!table.trim().isBlank()) {
            this.documentContents.add(DocumentContent.builder()
                    .contentId(this.documentContents.size())
                    .compareText(context)
                    .context(context)
                    .subDocumentContents(Collections.emptyList())
                    .type(DocumentContent.LineType.TABLE)
                    .build());
        }
    }

    /**
     * 이미지 데이터 등록
     */
    public void addImageContent(String text) {

        String context = text.trim();

        if (!text.trim().isBlank()) {
            this.documentContents.add(DocumentContent.builder()
                    .contentId(this.documentContents.size())
                    .compareText(context)
                    .context(context)
                    .subDocumentContents(Collections.emptyList())
                    .type(DocumentContent.LineType.IMAGE)
                    .build());
        }
    }
}
