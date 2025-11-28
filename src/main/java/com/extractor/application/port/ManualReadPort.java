package com.extractor.application.port;

import com.extractor.domain.model.Document;
import com.extractor.global.enums.ExtractType;

public interface ManualReadPort {

    /**
     * 메뉴얼 문서 조회
     *
     * @param manualId    메뉴얼 ID
     * @param extractType 표 추출 타입
     */
    Document getManualsPort(Long manualId, ExtractType extractType);
}
