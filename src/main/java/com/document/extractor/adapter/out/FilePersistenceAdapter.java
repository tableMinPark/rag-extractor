package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.constant.FileConst;
import com.document.extractor.adapter.out.entity.FileDetailEntity;
import com.document.extractor.adapter.out.entity.FileEntity;
import com.document.extractor.adapter.out.repository.FileDetailRepository;
import com.document.extractor.adapter.out.repository.FileRepository;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.FilePersistencePort;
import com.document.extractor.domain.model.FileDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public FileDetail saveFileDetailPort(FileDetail fileDetail) {

        FileDetailEntity fileDetailEntity;

        if (fileDetail.getFileDetailId() == null) {
            FileEntity fileEntity = fileRepository.save(FileEntity.builder()
                    .sysCreateUser(FileConst.FILE_PERSIST_USER)
                    .sysModifyUser(FileConst.FILE_PERSIST_USER)
                    .build());

            fileDetailEntity = fileDetailRepository.save(FileDetailEntity.fromDomain(fileEntity.getFileId(), fileDetail));

        } else {
            fileDetailEntity = fileDetailRepository.findById(fileDetail.getFileDetailId()).orElseThrow(NotFoundException::new);
            fileDetailEntity.update(fileDetail);
            fileDetailEntity = fileDetailRepository.save(fileDetailEntity);
        }

        return fileDetailEntity.toDomain();
    }

    /**
     * 파일 메타 정보 조회
     *
     * @param fileDetailId 파일 메타 정보 ID
     * @return 파일 메타 정보
     */
    @Transactional
    @Override
    public Optional<FileDetail> getFileDetailPort(Long fileDetailId) {
        return fileDetailRepository.findById(fileDetailId).map(FileDetailEntity::toDomain);
    }
}
