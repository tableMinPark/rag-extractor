package com.extractor.application.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChunkDocumentVo {

    private final OriginalDocumentVo originalDocumentVo;

    private final List<TrainingDocumentVo> trainingDocumentVos;
}
