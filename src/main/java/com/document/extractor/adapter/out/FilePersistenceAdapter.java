package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.FileDetailEntity;
import com.document.extractor.adapter.out.entity.FileEntity;
import com.document.extractor.adapter.out.repository.FileDetailRepository;
import com.document.extractor.adapter.out.repository.FileRepository;
import com.document.extractor.application.port.FilePersistencePort;
import com.document.extractor.domain.model.FileDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilePersistenceAdapter implements FilePersistencePort {

    private final FileRepository fileRepository;
    private final FileDetailRepository fileDetailRepository;

    /**
     * 파일 메타 정보 등록
     *
     * @param fileDetail 파일 메타 정보
     * @return 파일 메타 정보
     */
    @Transactional
    @Override
    public FileDetail createFileDetail(FileDetail fileDetail) {

        long fileId = fileRepository.save(FileEntity.builder()
                        .sysCreateUser("SYSTEM")
                        .build())
                .getFileId();

        FileDetailEntity fileDetailEntity = fileDetailRepository.save(FileDetailEntity.builder()
                .fileId(fileId)
                .originFileName(fileDetail.getOriginFileName())
                .fileName(fileDetail.getFileName())
                .ip(fileDetail.getIp())
                .filePath(fileDetail.getFilePath())
                .fileSize(fileDetail.getFileSize())
                .ext(fileDetail.getExt())
                .url(fileDetail.getUrl())
                .sysCreateUser("SYSTEM")
                .sysModifyUser("SYSTEM")
                .build());

        return FileDetail.builder()
                .fileDetailId(fileDetailEntity.getFileDetailId())
                .fileId(fileDetailEntity.getFileId())
                .originFileName(fileDetailEntity.getOriginFileName())
                .fileName(fileDetailEntity.getFileName())
                .ip(fileDetailEntity.getIp())
                .filePath(fileDetailEntity.getFilePath())
                .fileSize(fileDetailEntity.getFileSize())
                .ext(fileDetailEntity.getExt())
                .url(fileDetailEntity.getUrl())
                .sysCreateDt(fileDetailEntity.getSysCreateDt())
                .sysCreateUser(fileDetailEntity.getSysCreateUser())
                .sysModifyDt(fileDetailEntity.getSysModifyDt())
                .sysModifyUser(fileDetailEntity.getSysModifyUser())
                .build();
    }
}
