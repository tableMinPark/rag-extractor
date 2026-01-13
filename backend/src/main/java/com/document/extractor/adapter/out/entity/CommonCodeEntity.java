package com.document.extractor.adapter.out.entity;

import com.document.extractor.domain.model.CommonCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "GEN_COMMON_CODE")
public class CommonCodeEntity {

    @Id
    @Column(name = "code_id", nullable = false, updatable = false)
    @Comment("공통 코드 ID")
    private String codeId;

    @Column(name = "code", unique = true)
    @Comment("공통 코드")
    private String code;

    @Column(name = "code_name")
    @Comment("공통 코드명")
    private String codeName;

    @Column(name = "code_group")
    @Comment("그룹 코드")
    private String codeGroup;

    @Column(name = "sort_order")
    @Comment("정렬 필드")
    private Integer sortOrder;

    public CommonCode toDomain() {
        return CommonCode.builder()
                .codeId(codeId)
                .code(code)
                .codeName(codeName)
                .codeGroup(codeGroup)
                .sortOrder(sortOrder)
                .build();
    }
}
