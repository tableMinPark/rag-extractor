package com.extractor.adapter.out.dto.law.etc;

import com.extractor.adapter.out.dto.law.mapping.HangMappingDto;
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
public class HangDto {

    private String num;

    private String hang;

    private String content;

    private List<HoDto> hos;

    public static List<HangDto> toList(List<HangMappingDto> hangMappingDtos) {
        return hangMappingDtos.stream()
                .map(hangMappingDto -> {
                    String num = hangMappingDto.getNum() != null
                            ? StringUtil.getCircleNumber(hangMappingDto.getNum().replace(".", "").trim().charAt(0))
                            : "";
                    String hang = !num.isBlank()
                            ? "제" + num + "항"
                            : "";
                    String content = StringUtil.concat(hangMappingDto.getContents());

                    return HangDto.builder()
                            .num(num)
                            .hang(hang)
                            .content(!num.isBlank()
                                    ? content.replaceFirst(Pattern.quote(hangMappingDto.getNum()), "").trim()
                                    : content)
                            .hos(HoDto.toList(hangMappingDto.getHoMappingDtos()))
                            .build();
                })
                .toList();
    }
}
