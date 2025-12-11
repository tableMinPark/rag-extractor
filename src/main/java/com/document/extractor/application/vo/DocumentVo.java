package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class DocumentVo {

    private final String name;

    private final List<DocumentContentVo> documentContents;

    private final Boolean convertError;

    public static DocumentVo of(Document document) {
        return DocumentVo.builder()
                .name(document.getName())
                .documentContents(document.getDocumentContents().stream()
                        .map(DocumentContentVo::of)
                        .toList())
                .convertError(document.getConvertError())
                .build();
    }
}
