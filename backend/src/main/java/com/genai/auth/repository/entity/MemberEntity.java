package com.genai.auth.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "GEN_MEMBER")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(name = "email", unique = true)
    @Comment("이메일")
    private String email;

    @Column(name = "password")
    @Comment("비밀번호")
    private String password;

    @Column(name = "name")
    @Comment("회원명")
    private String name;

    @Column(name = "role", nullable = false)
    @Comment("회원 권한")
    private String role;

    @CreatedDate
    @Column(name = "sys_create_dt")
    @Comment("생성 일자")
    private LocalDateTime sysCreateDt;

    @LastModifiedDate
    @Column(name = "sys_modify_dt")
    @Comment("수정 일자")
    private LocalDateTime sysModifyDt;
}
