package com.document.extractor.application.vo;

import com.document.extractor.domain.model.DocumentContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class DocumentContentVo {

    private final String contentId;

    private final String compareText;

    private final String prefix;

    private final String title;

    private final String simpleTitle;

    private final String context;

    private final List<DocumentContentVo> subDocumentContents;

    private final String type;

    public static DocumentContentVo of(DocumentContent documentContent) {
        return DocumentContentVo.builder()
                .contentId(documentContent.getContentId())
                .compareText(documentContent.getCompareText())
                .prefix(documentContent.getPrefix())
                .title(documentContent.getTitle())
                .simpleTitle(documentContent.getSimpleTitle())
                .context(documentContent.getContext())
                .subDocumentContents(documentContent.getSubDocumentContents().stream()
                        .map(DocumentContentVo::of)
                        .toList())
                .type(documentContent.getType().name())
                .build();
    }
}
