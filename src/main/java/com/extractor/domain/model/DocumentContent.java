package com.extractor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Getter
public class DocumentContent {

    public enum LineType {
        TEXT, TABLE, IMAGE,
    }

    private final long contentId;

    private final String compareText;

    private String prefix;

    private String title;

    private String simpleTitle;

    private String context;

    private final List<DocumentContent> subDocumentContents;

    private final LineType type;

    @Builder
    public DocumentContent(long contentId, String compareText, String title, String simpleTitle, String context, List<DocumentContent> subDocumentContents, LineType type) {
        this.contentId = contentId;
        this.compareText = compareText;
        this.prefix = "";
        this.title = title == null ? "" : title;
        this.simpleTitle = simpleTitle == null ? "" : simpleTitle;
        this.context = context;
        this.subDocumentContents = subDocumentContents == null ? Collections.emptyList() : subDocumentContents;
        this.type = type;
    }

    /**
     * 타이틀 추출
     * @param prefixes 타이틀 정규식
     */
    public void extractTitle(List<String> prefixes) {
        for (String prefix : prefixes) {
            Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(this.compareText);

            if (matcher.find()) {
                this.prefix = prefix;
                this.title = matcher.group().trim();
                this.simpleTitle = matcher.group().trim();
                this.context = this.context.replaceFirst(prefix, "").strip();
            }
        }
    }
}
