package com.extractor.adapter.out.dto.law.etc;

import com.extractor.adapter.out.dto.law.mapping.ByeolpyoMappingDto;
import com.extractor.global.utils.StringUtil;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ByeolpyoDto {

    private String key;

    private String type;

    private String title;

    private String hwpFileName;

    private String hwpFileUrl;

    private String pdfFileName;

    private String pdfFileUrl;

    private String content;

    public static ByeolpyoDto of(ByeolpyoMappingDto.ByeolpyoMappingData byeolpyoMappingData) {
        return ByeolpyoDto.builder()
                .key(byeolpyoMappingData.getKey())
                .type(byeolpyoMappingData.getType())
                .title(byeolpyoMappingData.getTitle())
                .hwpFileName(byeolpyoMappingData.getHwpFileName())
                .hwpFileUrl(byeolpyoMappingData.getHwpFileUrl())
                .pdfFileName(byeolpyoMappingData.getPdfFileName())
                .pdfFileUrl(byeolpyoMappingData.getPdfFileUrl())
                .content(StringUtil.concat(byeolpyoMappingData.getContents()))
                .build();
    }

    /**
     * 별표 매핑 Dto -> 별표 Vo 변환
     * @param byeolpyoMappingDto 별표 매핑 Dto
     * @return 별표 Vo 목록
     */
    public static List<ByeolpyoDto> toList(ByeolpyoMappingDto byeolpyoMappingDto) {

        List<ByeolpyoDto> byeolpyoDtos = new ArrayList<>();

        if (byeolpyoMappingDto != null) {
            byeolpyoMappingDto.getData().forEach(byeolpyoData -> byeolpyoDtos.add(ByeolpyoDto.of(byeolpyoData)));
        }

        return byeolpyoDtos;
    }
}
