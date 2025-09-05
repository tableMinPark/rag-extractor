package com.extractor.adapter.in.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChunkLawRequestDto {

    private List<Long> lawIds;
}
