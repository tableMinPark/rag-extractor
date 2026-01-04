package com.document.extractor.adapter.in.exception;

import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InValidFieldResultDto {

    private Set<String> invalidFields;
}
