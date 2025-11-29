package com.extractor.application.port;

import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDetail;
import com.extractor.application.enums.ExtractType;

public interface ExtractPort {

    /**
     * 문서 추출
     *
     * @param fileDetail  원본 문서 정보
     * @return 문서
     */
    Document extractFilePort(FileDetail fileDetail);

    /**
     * 문서 텍스트 추출
     *
     * @param fileDetail 원본 문서 정보
     * @return 문서 텍스트
     */
    String extractTextPort(FileDetail fileDetail);
}
