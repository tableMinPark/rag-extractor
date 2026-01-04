package com.document.extractor.adapter.out.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "WN_FILE")
@Comment("파일 이력")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "WN_FILE_ID_SEQ",
        sequenceName = "WN_FILE_ID_SEQ",
        allocationSize = 1
)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WN_FILE_ID_SEQ")
    @Column(name = "file_id", nullable = false, updatable = false)
    @Comment("파일 ID")
    private Long fileId;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private List<FileDetailEntity> fileDetails;
}
