package com.extractor.domain.model;

import com.extractor.domain.vo.LawContentVo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
@Getter
public class LawDocument extends Document {

    private final Long lawId;

    private final List<LawContentVo> lawContents;

    @Builder
    public LawDocument(Long lawId, String lawName, List<LawContentVo> lawContents) {
        super(lawName);
        this.lawId = lawId;
        this.lawContents = lawContents;
        this.extract();
    }

    /**
     * 추출
     */
    public void extract() {
        this.clearDocumentContents();
        this.lawContents.forEach(lawContentVo -> {

            List<DocumentContent> subDocumentContents = new ArrayList<>();
            lawContentVo.getLawLinkVos().forEach(lawLinkVo -> {
                subDocumentContents.add(DocumentContent.builder()
                        .contentId(subDocumentContents.size())
                        .compareText(lawLinkVo.getText())
                        .title(lawLinkVo.getTitle())
                        .simpleTitle(lawLinkVo.getTitle())
                        .context(lawLinkVo.getContent())
                        .subDocumentContents(Collections.emptyList())
                        .type(DocumentContent.LineType.TEXT)
                        .build());
            });

            super.addTextContent(
                    lawContentVo.getTitle(),
                    lawContentVo.getSimpleTitle(),
                    lawContentVo.getCategoryCode(),
                    lawContentVo.getContent(),
                    subDocumentContents);
        });
    }
}