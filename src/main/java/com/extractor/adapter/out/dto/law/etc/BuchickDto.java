package com.extractor.adapter.out.dto.law.etc;

import com.extractor.adapter.out.dto.law.mapping.BuchickMappingDto;
import com.extractor.global.utils.StringUtil;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuchickDto {

    private String key;

    private String promulgationNum;

    private LocalDate promulgationDate;

    private String content;

    public static BuchickDto of(BuchickMappingDto.BuchickMappingData buchickMappingData) {
        return BuchickDto.builder()
                .key(buchickMappingData.getKey())
                .promulgationNum(buchickMappingData.getPromulgationNum())
                .promulgationDate(buchickMappingData.getPromulgationDate())
                .content(StringUtil.concat(buchickMappingData.getContents()))
                .build();
    }

    /**
     * 부칙 매핑 Dto -> 부칙 Dto 변환
     * @param buchickMappingDto 부칙 매핑 Dto
     * @return 부칙 Dto 목록
     */
    public static List<BuchickDto> toList(BuchickMappingDto buchickMappingDto) {

        List<BuchickDto> buchickDtos = new ArrayList<>();

        if (buchickMappingDto != null) {
            buchickMappingDto.getData().forEach(buchickData -> buchickDtos.add(BuchickDto.of(buchickData)));
        }

        return buchickDtos;
    }
}
