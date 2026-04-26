package com.genai.search.repository;

import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.wrapper.Rerank;
import com.genai.search.repository.wrapper.Search;

import java.util.List;

public interface SearchRepository {

    List<Search<DocumentEntity>> keywordSearch(String collectionId, String query, int topK,
                                               List<String> keywordFields, List<String> aliases);

    List<Search<DocumentEntity>> vectorSearch(String collectionId, String query, int topK,
                                              List<String> vectorFields, List<String> aliases);

    List<Rerank> rerank(String query, List<Rerank> documents);
}
