package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class FileDetail {

    private final Long fileDetailId;

    private final Long fileId;

    private final String originalFileName;

    private final String fileName;

    private final String ip;

    private final String filePath;

    private final Integer fileSize;

    private final String ext;

    private final String url;

    private final LocalDateTime sysCreateDt;

    private final String sysCreateUser;

    private final LocalDateTime sysModifyDt;

    private final String sysModifyUser;
}
