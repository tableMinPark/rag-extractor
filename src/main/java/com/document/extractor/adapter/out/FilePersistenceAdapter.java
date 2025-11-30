package com.document.extractor.adapter.out;

import com.document.extractor.application.port.FilePersistencePort;
import com.document.extractor.domain.model.FileDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilePersistenceAdapter implements FilePersistencePort {

    /**
     * 파일 메타 정보 등록
     *
     * @param fileDetail 파일 메타 정보
     * @return 파일 메타 정보
     */
    @Override
    public FileDetail createFileDetail(FileDetail fileDetail) {
        return null;
    }
}
