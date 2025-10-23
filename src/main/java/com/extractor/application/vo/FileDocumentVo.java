package com.extractor.application.vo;

import com.extractor.global.enums.FileExtension;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Getter
public class FileDocumentVo {

    private final String originalFileName;

    private final FileExtension extension;

    private final byte[] data;

    @Builder
    public FileDocumentVo(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.getOriginalFilename() == null) {
            throw new RuntimeException("multipart filename is null");
        }

        this.extension = FileExtension.find(multipartFile.getContentType());
        this.originalFileName = multipartFile.getOriginalFilename().replace("." + extension.getSimpleExtension(), "");

        try {
            this.data = multipartFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("get binary data error");
        }
    }
}