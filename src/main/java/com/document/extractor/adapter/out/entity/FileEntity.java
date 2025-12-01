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
@Table(name = "WN_FILE")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_FILE_FILE_ID_SEQ",
        sequenceName = "WN_FILE_FILE_ID_SEQ",
        allocationSize = 1
)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_FILE_DETAIL_FILE_DETAIL_ID_SEQ")
    @Column(name = "file_id", nullable = false)
    private Long fileId;

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
