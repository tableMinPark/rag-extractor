package com.document.extractor.application.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PageWrapper<T> {

    private List<T> data;

    private final boolean isLast;

    private final int page;

    private final int size;

    private final long totalCount;

    private final int totalPages;
}