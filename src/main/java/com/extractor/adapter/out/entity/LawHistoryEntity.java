package com.extractor.adapter.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_law_history")
public class LawHistoryEntity {

    @Id
    @Column(name = "seq_history")
    private Integer version;

    @Column(name = "seq", nullable = false)
    private Long lawId;

    @Column(name = "arrange")
    private Integer arrange;

    @Column(name = "iscurrent")
    private Integer isCurrent;

    @Column(name = "isdel")
    private String isDeleted;

    public Boolean getIsCurrent() {
        return isCurrent == 1;
    }
}
