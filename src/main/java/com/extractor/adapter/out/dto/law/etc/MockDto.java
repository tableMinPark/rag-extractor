package com.extractor.adapter.out.dto.law.etc;

import com.extractor.adapter.out.dto.law.mapping.MockMappingDto;
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
public class MockDto {

    private String num;

    private String mock;

    private String content;

    public static List<MockDto> toList(List<MockMappingDto> mockMappingDtos) {
        return mockMappingDtos.stream()
                .map(mockMappingDto -> {
                    String num = mockMappingDto.getNum() != null
                            ? mockMappingDto.getNum().replace(".", "").trim()
                            : "";
                    String mock = !num.isBlank()
                            ? "제" + num + "목"
                            : "";
                    String content = StringUtil.concat(mockMappingDto.getContents());

                    return MockDto.builder()
                            .num(num)
                            .mock(mock)
                            .content(!num.isBlank()
                                    ? content.replaceFirst(Pattern.quote(mockMappingDto.getNum()), "").trim()
                                    : content)
                            .build();
                })
                .toList();
    }
}
