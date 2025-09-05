package com.extractor.adapter.in.dto.response;

import com.extractor.domain.model.DocumentLine;
import com.extractor.domain.model.PassageDocument;
import com.extractor.domain.vo.pattern.ChunkPatternVo;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResponseDto {

    private ChunkPatternVo pattern;

    private List<PassageDocument> passages;

    private List<DocumentLine> lines;
}
