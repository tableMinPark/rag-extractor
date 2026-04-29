package com.genai.extractor.service;

import com.genai.extractor.repository.entity.FileDetailEntity;
import com.genai.extractor.repository.entity.FileEntity;
import com.genai.extractor.repository.FileDetailRepository;
import com.genai.extractor.repository.FileRepository;
import com.genai.common.exception.NotFoundException;
import com.genai.common.vo.UploadFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileDetailService {

    private final FileRepository fileRepository;
    private final FileDetailRepository fileDetailRepository;

    @Transactional
    public FileDetailEntity saveFileDetail(UploadFile uploadFile, String sysUser) {
        FileEntity fileEntity = fileRepository.save(FileEntity.builder()
                .sysCreateUser(sysUser)
                .sysModifyUser(sysUser)
                .build());

        return fileDetailRepository.save(FileDetailEntity.builder()
                .fileId(fileEntity.getFileId())
                .originFileName(uploadFile.getOriginFileName())
                .fileName(uploadFile.getFileName())
                .ip(uploadFile.getIp())
                .filePath(uploadFile.getFilePath())
                .fileSize(uploadFile.getFileSize())
                .ext(uploadFile.getExt())
                .url(uploadFile.getUrl())
                .sysCreateUser(sysUser)
                .sysModifyUser(sysUser)
                .build());
    }

    @Transactional(readOnly = true)
    public FileDetailEntity getFileDetail(Long fileDetailId) {
        return fileDetailRepository.findById(fileDetailId)
                .orElseThrow(() -> new NotFoundException("파일 메타 정보"));
    }
}
