package com.extractor.application.port;

import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;

public interface FilePort {

    /**
     * 파일 업로드
     * @param originalDocumentVo 원본 문서 Vo
     * @return 원본 문서 도메인 객체
     */
    OriginalDocument uploadFilePort(OriginalDocumentVo originalDocumentVo);

    /**
     * 파일 정리
     * @param originalDocument 원본 문서 도메인 객체
     */
    void clearFilePort(OriginalDocument originalDocument);
}
