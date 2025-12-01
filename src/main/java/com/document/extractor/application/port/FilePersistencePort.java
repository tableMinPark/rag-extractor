package com.document.extractor.application.port;

import com.document.extractor.domain.model.FileDetail;

import java.util.Optional;

public interface FilePersistencePort {

    /**
     * 파일 메타 정보 등록
     *
     * @param fileDetail 파일 메타 정보
     * @return 파일 메타 정보
     */
    FileDetail saveFileDetailPort(FileDetail fileDetail);

    /**
     * 파일 메타 정보 조회
     *
     * @param fileDetailId 파일 메타 정보 ID
     * @return 파일 메타 정보
     */
    Optional<FileDetail> getFileDetailPort(Long fileDetailId);
}
