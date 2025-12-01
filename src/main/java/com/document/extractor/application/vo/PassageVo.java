package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Passage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PassageVo {

    private Long passageId;

    private Long sourceId;

    private Long version;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer contentTokenSize;

    private Integer subContentTokenSize;

    private LocalDateTime sysCreateDt;

    private LocalDateTime sysModifyDt;

    public static PassageVo of(Passage passage) {
        return PassageVo.builder()
                .passageId(passage.getPassageId())
                .sourceId(passage.getSourceId())
                .version(passage.getVersion())
                .title(passage.getTitle())
                .subTitle(passage.getSubTitle())
                .thirdTitle(passage.getThirdTitle())
                .content(passage.getContent())
                .subContent(passage.getSubContent())
                .contentTokenSize(passage.getContent().length())
                .subContentTokenSize(passage.getSubContent().length())
                .sysCreateDt(passage.getSysCreateDt())
                .sysModifyDt(passage.getSysModifyDt())
                .build();
    }
}
