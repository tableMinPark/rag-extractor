package com.document.extractor.domain.model;

import com.document.global.utils.HtmlUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class Passage {

    private final Long passageId;

    private Long sourceId;

    private Long version;

    private String title;

    private final String subTitle;

    private final String thirdTitle;

    private final String content;

    private final String subContent;

    private final Integer tokenSize;

    private final LocalDateTime sysCreateDt;

    private final LocalDateTime sysModifyDt;

    public void update(Long sourceId, Long version, String title) {
        this.sourceId = sourceId;
        this.version = version;
        this.title = title;
    }

    /**
     * 토큰 수 기준 청킹 처리
     *
     * @param tokenSize   청킹 사이즈
     * @param overlapSize 오버랩 사이즈
     */
    public List<Chunk> chunking(int tokenSize, int overlapSize) {

        List<Chunk> chunks = new ArrayList<>();

        if (0 < tokenSize && tokenSize < this.tokenSize) {
            int step = tokenSize - overlapSize;

            for (int start = 0; start < content.length(); start += step) {
                int end = Math.min(content.length(), start + tokenSize);
                String content = this.content.substring(start, end);
                String compactContent = HtmlUtil.convertTableHtmlToMarkDown(content);

                chunks.add(Chunk.builder()
                        .passageId(this.passageId)
                        .version(this.version)
                        .title(this.title)
                        .subTitle(this.subTitle)
                        .thirdTitle(this.thirdTitle)
                        .content(content)
                        .subContent(this.subContent)
                        .compactContent(compactContent)
                        .tokenSize(compactContent.length())
                        .sysCreateDt(this.sysCreateDt)
                        .sysModifyDt(this.sysModifyDt)
                        .build());
            }
        } else {
            String compactContent = HtmlUtil.convertTableHtmlToMarkDown(this.content);
            chunks.add(Chunk.builder()
                    .passageId(this.passageId)
                    .version(this.version)
                    .title(this.title)
                    .subTitle(this.subTitle)
                    .thirdTitle(this.thirdTitle)
                    .content(this.content)
                    .subContent(this.subContent)
                    .compactContent(compactContent)
                    .tokenSize(compactContent.length())
                    .sysCreateDt(this.sysCreateDt)
                    .sysModifyDt(this.sysModifyDt)
                    .build());
        }

        return chunks;
    }
}
