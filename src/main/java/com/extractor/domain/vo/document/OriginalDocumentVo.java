package com.extractor.domain.vo.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Getter
public class OriginalDocumentVo {

    private final String originalFileName;

    private final String extension;

    private final byte[] data;

    @Builder
    public OriginalDocumentVo(MultipartFile multipartFile) {
        try {
            if (multipartFile == null || multipartFile.getOriginalFilename() == null) {
                throw new RuntimeException("multipart filename is null");
            }

            this.originalFileName = multipartFile.getOriginalFilename();
            this.extension = multipartFile.getContentType();
            this.data = multipartFile.getBytes();
        }  catch (IOException e) {
            throw new RuntimeException("get binary data error");
        }
    }
}
