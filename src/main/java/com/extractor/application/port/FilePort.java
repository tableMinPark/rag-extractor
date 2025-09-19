package com.extractor.application.port;

import com.extractor.domain.model.FileDocument;
import com.extractor.domain.vo.FileDocumentVo;

public interface FilePort {

    /**
     * 파일 업로드
     *
     * @param fileDocumentVo 원본 문서 Vo
     * @return 원본 문서 도메인 객체
     */
    FileDocument uploadFilePort(FileDocumentVo fileDocumentVo);

    /**
     * 파일 정리
     *
     * @param fileDocument 원본 문서 도메인 객체
     */
    void clearFilePort(FileDocument fileDocument);
}