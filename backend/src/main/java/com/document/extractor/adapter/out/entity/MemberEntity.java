package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "GEN_MEMBER")
@Comment("파일 이력")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "GEN_MEMBER_ID_SEQ",
        sequenceName = "GEN_MEMBER_ID_SEQ",
        allocationSize = 1
)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GEN_MEMBER_ID_SEQ")
    @Column(name = "member_id", nullable = false, updatable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(name = "name")
    @Comment("회원명")
    private String name;

    @CreatedDate
    @Column(name = "password")
    @Comment("비밀 번호")
    private String password;

    @CreatedDate
    @Column(name = "role")
    @Comment("회원 권한")
    private String role;

    public Member toDomain() {
        return Member.builder()
                .memberId(memberId)
                .name(name)
                .password(password)
                .role(role)
                .build();
    }
}
