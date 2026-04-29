package com.genai.search.service;

import com.genai.search.controller.dto.request.SearchRequestDto;
import com.genai.search.controller.dto.response.SearchResponseDto;
import com.genai.search.repository.SearchRepository;
import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.wrapper.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final List<String> KEYWORD_FIELDS = List.of("title", "sub_title", "third_title", "content");
    private static final List<String> VECTOR_FIELDS = List.of("vector-context");

    private final SearchRepository searchRepository;

    public List<SearchResponseDto> keywordSearch(SearchRequestDto request) {
        List<Search<DocumentEntity>> results = searchRepository.keywordSearch(
                request.getCollectionId(),
                request.getQuery(),
                request.getTopK(),
                KEYWORD_FIELDS,
                request.getAliases()
        );
        return SearchResponseDto.fromList(results);
    }

    public List<SearchResponseDto> vectorSearch(SearchRequestDto request) {
        List<Search<DocumentEntity>> results = searchRepository.vectorSearch(
                request.getCollectionId(),
                request.getQuery(),
                request.getTopK(),
                VECTOR_FIELDS,
                request.getAliases()
        );
        return SearchResponseDto.fromList(results);
    }
}
