package com.extractor.application.port;

import com.extractor.domain.model.Document;
import com.extractor.global.enums.ExtractType;

public interface LawReadPort {

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    Document getLawsPort(Long lawId, ExtractType extractType);
}