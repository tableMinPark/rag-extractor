package com.extractor.adapter.in.dto;

import com.extractor.domain.model.DocumentLine;
import com.extractor.domain.model.PassageDocument;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResponseDto {

    private List<PassageDocument> passages;

    private List<DocumentLine> lines;
}
