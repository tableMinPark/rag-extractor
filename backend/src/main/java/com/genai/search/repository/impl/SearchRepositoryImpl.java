package com.genai.search.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genai.common.exception.SearchErrorException;
import com.genai.embed.config.properties.EmbedProperty;
import com.genai.search.config.properties.RerankerProperty;
import com.genai.search.config.properties.SearchProperty;
import com.genai.search.repository.SearchRepository;
import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.request.KeywordSearchRequest;
import com.genai.search.repository.request.RerankRequest;
import com.genai.search.repository.request.VectorSearchRequest;
import com.genai.search.repository.response.RerankResponse;
import com.genai.search.repository.response.SearchResponse;
import com.genai.search.repository.vo.ConvertVectorVO;
import com.genai.search.repository.wrapper.Rerank;
import com.genai.search.repository.wrapper.Search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SearchRepositoryImpl implements SearchRepository {

    private final WebClient searchWebClient;
    private final WebClient rerankerWebClient;
    private final WebClient embedWebClient;
    private final SearchProperty searchProperty;
    private final RerankerProperty rerankerProperty;
    private final EmbedProperty embedProperty;
    private final ObjectMapper objectMapper;

    public SearchRepositoryImpl(
            @Qualifier("searchWebClient") WebClient searchWebClient,
            @Qualifier("rerankerWebClient") WebClient rerankerWebClient,
            @Qualifier("embedWebClient") WebClient embedWebClient,
            @Autowired SearchProperty searchProperty,
            @Autowired RerankerProperty rerankerProperty,
            @Autowired EmbedProperty embedProperty,
            @Autowired ObjectMapper objectMapper
    ) {
        this.searchWebClient = searchWebClient;
        this.rerankerWebClient = rerankerWebClient;
        this.embedWebClient = embedWebClient;
        this.searchProperty = searchProperty;
        this.rerankerProperty = rerankerProperty;
        this.embedProperty = embedProperty;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Search<DocumentEntity>> keywordSearch(String collectionId, String query, int topK,
                                                      List<String> keywordFields, List<String> aliases) {
        KeywordSearchRequest request = KeywordSearchRequest.builder()
                .size(topK)
                .sort(List.of(KeywordSearchRequest.sort(KeywordSearchRequest.SortField._score, "desc")))
                .query(KeywordSearchRequest.query(KeywordSearchRequest.QueryType.best_fields, query, keywordFields, aliases))
                .build();

        ResponseEntity<String> responseEntity = searchWebClient.post()
                .uri(searchProperty.getUrl() + "/" + collectionId + "/_search")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new SearchErrorException("키워드 검색 실패 (" + collectionId + ")");
        }

        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(SearchResponse.class, DocumentEntity.class);
            SearchResponse<DocumentEntity> responseBody = objectMapper.readValue(responseEntity.getBody(), type);

            if (responseBody == null) {
                throw new SearchErrorException("키워드 검색 응답 바디 조회 실패 (" + collectionId + ")");
            }

            return responseBody.getResult().hits();
        } catch (JsonProcessingException e) {
            throw new SearchErrorException("키워드 검색 응답 바디 변환 실패 (" + collectionId + ")");
        }
    }

    @Override
    public List<Search<DocumentEntity>> vectorSearch(String collectionId, String query, int topK,
                                                     List<String> vectorFields, List<String> aliases) {
        ConvertVectorVO convertVectorVO = ConvertVectorVO.builder()
                .id(Long.MIN_VALUE)
                .content(query)
                .build();

        convertVectorVO = embedWebClient.post()
                .uri(embedProperty.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(convertVectorVO))
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                        response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .flatMap(errorBody -> Mono.error(
                                        new SearchErrorException("벡터 검색 실패 (" + collectionId + ")"))))
                .bodyToMono(new ParameterizedTypeReference<List<ConvertVectorVO>>() {})
                .blockOptional()
                .orElseThrow(() -> new SearchErrorException("벡터 검색 실패 (" + collectionId + ")"))
                .getFirst();

        VectorSearchRequest request = VectorSearchRequest.builder()
                .size(topK)
                .query(VectorSearchRequest.query(vectorFields, convertVectorVO.getVector(), aliases))
                .build();

        ResponseEntity<String> responseEntity = searchWebClient.post()
                .uri(searchProperty.getUrl() + "/" + collectionId + "/_search")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new SearchErrorException("벡터 검색 실패 (" + collectionId + ")");
        }

        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(SearchResponse.class, DocumentEntity.class);
            SearchResponse<DocumentEntity> responseBody = objectMapper.readValue(responseEntity.getBody(), type);

            if (responseBody == null) {
                throw new SearchErrorException("벡터 검색 응답 바디 조회 실패 (" + collectionId + ")");
            }

            return responseBody.getResult().hits();
        } catch (JsonProcessingException e) {
            throw new SearchErrorException("벡터 검색 응답 바디 변환 실패 (" + collectionId + ")");
        }
    }

    @Override
    public List<Rerank> rerank(String query, List<Rerank> documents) {
        if (documents.isEmpty()) return documents;

        RerankRequest requestBody = RerankRequest.builder()
                .query(query)
                .documents(documents.stream()
                        .map(rerank -> RerankRequest.Document.builder()
                                .id(String.valueOf(rerank.getDocument().getChunkId()))
                                .content(rerank.getDocument().getContext())
                                .build())
                        .toList())
                .build();

        ResponseEntity<RerankResponse> responseEntity = rerankerWebClient.post()
                .uri(rerankerProperty.getUrl())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchangeToMono(response -> response.bodyToMono(RerankResponse.class)
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new SearchErrorException("리랭킹 실패");
        }

        RerankResponse responseBody = responseEntity.getBody();
        if (responseBody == null) throw new SearchErrorException("리랭킹 응답 바디 조회 실패");

        Map<Long, Rerank> documentMap = documents.stream()
                .collect(Collectors.toMap(rerank -> rerank.getDocument().getChunkId(), d -> d));

        List<Rerank> rerankDocuments = new ArrayList<>();
        responseBody.getDocuments().forEach(doc -> {
            Long chunkId = Long.parseLong(doc.id());
            if (documentMap.containsKey(chunkId)) {
                Rerank rerankDocument = documentMap.get(chunkId);
                rerankDocument.setRerankScore(doc.score());
                rerankDocuments.add(rerankDocument);
            }
        });

        return rerankDocuments;
    }
}
