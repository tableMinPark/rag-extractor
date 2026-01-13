package com.document.extractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Member {

    private final Long memberId;

    private final String name;

    private final String password;

    private final String role;
}
