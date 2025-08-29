package com.extractor.adapter.in.dto;

import com.extractor.domain.model.DocumentLine;
import com.extractor.domain.model.PassageDocument;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExtractHwpxResponseDto {

    private List<PassageDocument> passages;

    private List<DocumentLine> lines;

    @Builder
    public ExtractHwpxResponseDto(List<PassageDocument> passages, List<DocumentLine> lines) {
        this.passages = passages;
        this.lines = lines;
    }
}
