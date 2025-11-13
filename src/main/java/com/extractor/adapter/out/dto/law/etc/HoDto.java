package com.extractor.adapter.out.dto.law.etc;

import com.extractor.adapter.out.dto.law.mapping.HoMappingDto;
import com.extractor.global.utils.StringUtil;
import lombok.*;

import java.util.List;
import java.util.regex.Pattern;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoDto {

    private String num;

    private String ho;

    private String content;

    private List<MockDto> mocks;

    public static List<HoDto> toList(List<HoMappingDto> hoMappingDtos) {
        return hoMappingDtos.stream()
                .map(hoMappingDto -> {
                    String num = hoMappingDto.getNum() != null
                            ? hoMappingDto.getNum().replace(".", "").trim()
                            : "";
                    String ho =  !num.isBlank()
                            ? "제" + num + "호"
                            : "";
                    String content = StringUtil.concat(hoMappingDto.getContents());

                    return HoDto.builder()
                            .num(num)
                            .ho(ho)
                            .content(!num.isBlank()
                                    ? content.replaceFirst(Pattern.quote(hoMappingDto.getNum()), "").trim()
                                    : content)
                            .mocks(MockDto.toList(hoMappingDto.getMockMappingDtos()))
                            .build();
                })
                .toList();
    }
}
