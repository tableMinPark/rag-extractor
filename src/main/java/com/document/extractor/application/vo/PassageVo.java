package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Passage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PassageVo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long passageId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long sourceId;

    private Long version;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer contentTokenSize;

    private Integer subContentTokenSize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime sysCreateDt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
