package com.extractor.application.port;

import com.extractor.domain.model.FileDetail;

public interface FilePersistencePort {

    /**
     * 파일 메타 정보 등록
     *
     * @param fileDetail 파일 메타 정보
     * @return 파일 메타 정보
     */
    FileDetail createFileDetail(FileDetail fileDetail);
}
