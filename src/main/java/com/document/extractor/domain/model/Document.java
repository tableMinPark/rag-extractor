package com.document.extractor.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class Document {

    private String name;

    private List<DocumentContent> documentContents;

    private Boolean convertError = false;

    public Document(String name, boolean convertError) {
        this(name, new ArrayList<>(), convertError);
    }

    public Document(String name, List<DocumentContent> documentContents, boolean convertError) {
        this.name = name;
        this.documentContents = documentContents;
        this.convertError = convertError;
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
