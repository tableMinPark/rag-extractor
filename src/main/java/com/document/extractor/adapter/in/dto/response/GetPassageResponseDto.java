package com.document.extractor.adapter.in.dto.response;

import com.document.extractor.application.vo.PassageVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetPassageResponseDto {

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

    public static GetPassageResponseDto of(PassageVo passageVo) {
        return GetPassageResponseDto.builder()
                .passageId(passageVo.getPassageId())
                .sourceId(passageVo.getSourceId())
                .version(passageVo.getVersion())
                .title(passageVo.getTitle())
                .subTitle(passageVo.getSubTitle())
                .thirdTitle(passageVo.getThirdTitle())
                .content(passageVo.getContent())
                .subContent(passageVo.getSubContent())
                .contentTokenSize(passageVo.getContentTokenSize())
                .subContentTokenSize(passageVo.getSubContentTokenSize())
                .build();
    }

    public static List<GetPassageResponseDto> toList(List<PassageVo> passageVos) {
        return passageVos.stream()
                .map(GetPassageResponseDto::of)
                .toList();
    }
}
