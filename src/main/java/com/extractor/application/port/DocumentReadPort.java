package com.extractor.application.port;

import com.extractor.application.enums.ExtractType;
import com.extractor.domain.model.Document;

public interface DocumentReadPort {

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    Document getLawsPort(Long lawId, ExtractType extractType);

    /**
     * 메뉴얼 문서 조회
     *
     * @param manualId    메뉴얼 ID
     * @param extractType 표 추출 타입
     */
    Document getManualsPort(Long manualId, ExtractType extractType);
}
