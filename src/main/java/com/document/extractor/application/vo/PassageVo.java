package com.document.extractor.application.vo;

import com.document.extractor.domain.model.Passage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class PassageVo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long passageId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long sourceId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long version;

    private String title;

    private String subTitle;

    private String thirdTitle;

    private String content;

    private String subContent;

    private Integer contentTokenSize;

    private Integer subContentTokenSize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sysModifyDt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updateState;

    private Integer sortOrder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer parentSortOrder;

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
                .updateState(passage.getUpdateState().getCode())
                .sortOrder(passage.getSortOrder())
                .parentSortOrder(passage.getParentSortOrder())
                .build();
    }
}
