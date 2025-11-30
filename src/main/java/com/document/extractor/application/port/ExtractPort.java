package com.document.extractor.application.port;

import com.document.extractor.domain.model.Document;
import com.document.extractor.domain.model.FileDetail;
import com.document.global.enums.ExtractType;

public interface ExtractPort {

    /**
     * 문서 추출
     *
     * @param fileDetail  원본 문서 정보
     * @param extractType 표 추출 타입
     * @return 문서
     */
    Document extractFilePort(FileDetail fileDetail, ExtractType extractType);

    /**
     * 문서 텍스트 추출
     *
     * @param fileDetail 원본 문서 정보
     * @return 문서 텍스트
     */
    String extractTextPort(FileDetail fileDetail);
}
