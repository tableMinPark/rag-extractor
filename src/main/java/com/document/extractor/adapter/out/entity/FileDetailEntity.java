package com.document.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.*;
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
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_FILE_DETAIL_FILE_DETAIL_ID_SEQ",
        sequenceName = "WN_FILE_DETAIL_FILE_DETAIL_ID_SEQ",
        allocationSize = 1
)
public class FileDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_FILE_DETAIL_FILE_DETAIL_ID_SEQ")
    @Column(name = "file_detail_id", nullable = false)
    private Long fileDetailId;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "file_origin_name")
    private String originFileName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "ip")
    private String ip;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "ext")
    private String ext;

    @Column(name = "url")
    private String url;

    @CreatedDate
    @Column(name = "sys_create_dt")
    private LocalDateTime sysCreateDt;

    @Column(name = "sys_create_user")
    private String sysCreateUser;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    private LocalDateTime sysModifyDt;

    @Column(name = "sys_modify_user")
    private String sysModifyUser;
}
