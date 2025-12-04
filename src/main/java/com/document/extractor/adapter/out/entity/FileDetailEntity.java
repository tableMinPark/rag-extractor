package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.FileDetail;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_FILE_DETAIL")
@Comment("파일 상세")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_FILE_DETAIL_ID_SEQ",
        sequenceName = "WN_FILE_DETAIL_ID_SEQ",
        allocationSize = 1
)
public class FileDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_FILE_DETAIL_ID_SEQ")
    @Column(name = "file_detail_id", nullable = false, updatable = false)
    @Comment("파일 상세 ID")
    private Long fileDetailId;

    @Column(name = "file_id")
    @Comment("파일 ID")
    private Long fileId;

    @Column(name = "file_origin_name")
    @Comment("원본 파일명")
    private String originFileName;

    @Column(name = "file_name")
    @Comment("저장 파일명")
    private String fileName;

    @Column(name = "ip")
    @Comment("저장 서버 IP")
    private String ip;

    @Column(name = "file_path")
    @Comment("파일 상대 경로")
    private String filePath;

    @Column(name = "file_size")
    @Comment("파일 크기")
    private Integer fileSize;

    @Column(name = "ext")
    @Comment("파일 확장자")
    private String ext;

    @Column(name = "url")
    @Comment("파일 접근 경로")
    private String url;

    @CreatedDate
    @Column(name = "sys_create_dt")
    @Comment("생성 일자")
    private LocalDateTime sysCreateDt;

    @Column(name = "sys_create_user")
    @Comment("생성지")
    private String sysCreateUser;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    @Comment("수정 일자")
    private LocalDateTime sysModifyDt;

    @Column(name = "sys_modify_user")
    @Comment("수정자")
    private String sysModifyUser;

    public void update(FileDetail fileDetail) {
        this.fileId = fileDetail.getFileId();
        this.originFileName = fileDetail.getOriginFileName();
        this.fileName = fileDetail.getFileName();
        this.ip = fileDetail.getIp();
        this.filePath = fileDetail.getFilePath();
        this.fileSize = fileDetail.getFileSize();
        this.ext = fileDetail.getExt();
        this.url = fileDetail.getUrl();
        this.sysCreateUser = fileDetail.getSysCreateUser();
        this.sysModifyUser = fileDetail.getSysModifyUser();
    }

    public FileDetail toDomain() {
        return FileDetail.builder()
                .fileDetailId(fileDetailId)
                .fileId(fileId)
                .originFileName(originFileName)
                .fileName(fileName)
                .ip(ip)
                .filePath(filePath)
                .fileSize(fileSize)
                .ext(ext)
                .url(url)
                .sysCreateDt(sysCreateDt)
                .sysCreateUser(sysCreateUser)
                .sysModifyDt(sysModifyDt)
                .sysModifyUser(sysModifyUser)
                .build();
    }

    public static FileDetailEntity fromDomain(Long fileId, FileDetail fileDetail) {
        return FileDetailEntity.builder()
                .fileId(fileId)
                .originFileName(fileDetail.getOriginFileName())
                .fileName(fileDetail.getFileName())
                .ip(fileDetail.getIp())
                .filePath(fileDetail.getFilePath())
                .fileSize(fileDetail.getFileSize())
                .ext(fileDetail.getExt())
                .url(fileDetail.getUrl())
                .sysCreateUser(fileDetail.getSysCreateUser())
                .sysModifyUser(fileDetail.getSysModifyUser())
                .build();
    }
}
