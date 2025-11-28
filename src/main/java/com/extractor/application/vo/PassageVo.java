package com.extractor.application.vo;

import com.extractor.domain.model.Passage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PassageVo {

    private Long passageId;

    private Long sourceId;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer tokenSize;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;

    @JsonIgnore
    private final List<ChunkVo> chunkVos = new ArrayList<>();

    /**
     * 토큰 수 기준 청킹 처리
     *
     * @param tokenSize   청킹 사이즈
     * @param overlapSize 오버랩 사이즈
     */
    public PassageVo chunking(int tokenSize, int overlapSize) {

        if (0 < tokenSize && tokenSize < this.tokenSize) {
            int step = tokenSize - overlapSize;

            for (int start = 0; start < content.length(); start += step) {
                int end = Math.min(content.length(), start + tokenSize);

                String content = this.content.substring(start, end);

                chunkVos.add(ChunkVo.builder()
                        .passageId(this.passageId)
                        .title(this.title)
                        .subTitle(this.subTitle)
                        .thirdTitle(this.thirdTitle)
                        .content(content)
                        .subContent(this.subContent)
                        .tokenSize(content.length())
                        .sysCreateDt(this.sysCreateDt)
                        .sysModifyDt(this.sysModifyDt)
                        .build());
            }
        } else {
            chunkVos.add(ChunkVo.builder()
                    .passageId(this.passageId)
                    .title(this.title)
                    .subTitle(this.subTitle)
                    .thirdTitle(this.thirdTitle)
                    .content(this.content)
                    .subContent(this.subContent)
                    .tokenSize(this.tokenSize)
                    .sysCreateDt(this.sysCreateDt)
                    .sysModifyDt(this.sysModifyDt)
                    .build());
        }

        return this;
    }

    public static PassageVo of(Passage passage) {
        return PassageVo.builder()
                .passageId(passage.getPassageId())
                .sourceId(passage.getSourceId())
                .title(passage.getTitle())
                .subTitle(passage.getSubTitle())
                .thirdTitle(passage.getThirdTitle())
                .content(passage.getContent())
                .subContent(passage.getSubContent())
                .tokenSize(passage.getTokenSize())
                .sysCreateDt(passage.getSysCreateDt())
                .sysModifyDt(passage.getSysModifyDt())
                .build();
    }
}
