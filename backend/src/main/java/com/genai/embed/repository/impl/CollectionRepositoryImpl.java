package com.genai.embed.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genai.embed.config.properties.CollectionProperty;
import com.genai.embed.config.properties.EmbedProperty;
import com.genai.embed.config.properties.IndexerProperty;
import com.genai.embed.repository.CollectionRepository;
import com.genai.embed.repository.entity.CollectionEntity;
import com.genai.embed.repository.entity.DocumentEntity;
import com.genai.embed.repository.request.CreateIndexBulkRequest;
import com.genai.embed.repository.request.DeleteIndexBulkRequest;
import com.genai.embed.repository.response.CreateIndexBulkResponse;
import com.genai.embed.repository.response.DeleteIndexBulkResponse;
import com.genai.embed.repository.response.GetCollectionResponse;
import com.genai.embed.repository.vo.ConvertVectorVO;
import com.genai.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CollectionRepositoryImpl implements CollectionRepository {

    private final WebClient collectionWebClient;
    private final WebClient indexerWebClient;
    private final WebClient embedWebClient;
    private final CollectionProperty collectionProperty;
    private final IndexerProperty indexerProperty;
    private final EmbedProperty embedProperty;
    private final ObjectMapper objectMapper;

    public CollectionRepositoryImpl(
            @Qualifier("collectionWebClient") WebClient collectionWebClient,
            @Qualifier("indexerWebClient") WebClient indexerWebClient,
            @Qualifier("embedWebClient") WebClient embedWebClient,
            CollectionProperty collectionProperty,
            IndexerProperty indexerProperty,
            EmbedProperty embedProperty,
            ObjectMapper objectMapper
    ) {
        this.collectionWebClient = collectionWebClient;
        this.indexerWebClient = indexerWebClient;
        this.embedWebClient = embedWebClient;
        this.collectionProperty = collectionProperty;
        this.indexerProperty = indexerProperty;
        this.embedProperty = embedProperty;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CollectionEntity> findCollectionByCollectionId(String collectionId) {
        ResponseEntity<Map<String, Object>> responseEntity = collectionWebClient.get()
                .uri(collectionProperty.getUrl() + "/" + collectionId)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
            return Optional.empty();
        }

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody == null || !responseBody.containsKey(collectionId)) {
            return Optional.empty();
        }

        GetCollectionResponse response = objectMapper.convertValue(responseBody.get(collectionId), GetCollectionResponse.class);

        return Optional.of(CollectionEntity.builder()
                .collectionId(collectionId)
                .numOfShards(Integer.parseInt(response.getSettings().getIndex().getNumberOfShards()))
                .numOfReplication(Integer.parseInt(response.getSettings().getIndex().getNumberOfReplicas()))
                .fields(response.getMappings().getProperties().keySet().stream().toList())
                .build());
    }

    @Override
    public List<DocumentEntity> convertVector(String collectionId, List<DocumentEntity> documentEntities) {
        if (documentEntities.isEmpty()) return documentEntities;

        List<ConvertVectorVO> convertVectorVos = documentEntities.stream()
                .map(doc -> ConvertVectorVO.builder()
                        .id(doc.getChunkId())
                        .content(doc.getContext())
                        .build())
                .toList();

        convertVectorVos = embedWebClient.post()
                .uri(embedProperty.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(convertVectorVos)
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::isError, response ->
                        response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .flatMap(err -> Mono.error(new RuntimeException("벡터 변환 실패 (" + collectionId + ")"))))
                .bodyToMono(new ParameterizedTypeReference<List<ConvertVectorVO>>() {})
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("벡터 변환 응답 없음 (" + collectionId + ")"));

        Map<Long, ConvertVectorVO> vectorMap = convertVectorVos.stream()
                .collect(Collectors.toMap(ConvertVectorVO::getId, v -> v));

        return documentEntities.stream()
                .peek(doc -> {
                    ConvertVectorVO vo = vectorMap.get(doc.getChunkId());
                    doc.setContextVector(vo != null ? vo.getVector() : Collections.emptyList());
                })
                .toList();
    }

    @Override
    public void createIndex(String collectionId, List<DocumentEntity> documentEntities) {
        if (documentEntities.isEmpty()) return;

        int batchSize = 500;
        for (int batchIndex = 0; batchIndex < documentEntities.size(); batchIndex += batchSize) {
            StringBuilder body = new StringBuilder();

            for (int i = batchIndex; i < Math.min(documentEntities.size(), batchIndex + batchSize); i++) {
                try {
                    DocumentEntity doc = documentEntities.get(i);
                    CreateIndexBulkRequest req = CreateIndexBulkRequest.builder()
                            .index(CreateIndexBulkRequest.Index.builder()
                                    .collectionId(collectionId)
                                    .id(String.valueOf(doc.getChunkId()))
                                    .build())
                            .build();
                    body.append(objectMapper.writeValueAsString(req)).append("\n");
                    body.append(objectMapper.writeValueAsString(doc)).append("\n");
                } catch (JsonProcessingException ignored) {
                }
            }

            CreateIndexBulkResponse response = indexerWebClient.put()
                    .uri(indexerProperty.getUrl() + "/_bulk")
                    .contentType(MediaType.APPLICATION_NDJSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body.toString())
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::isError, resp ->
                            resp.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                    .flatMap(err -> Mono.error(new RuntimeException("데이터 색인 실패 (" + collectionId + ")"))))
                    .bodyToMono(CreateIndexBulkResponse.class)
                    .blockOptional()
                    .orElseThrow(() -> new RuntimeException("데이터 색인 실패 (" + collectionId + ")"));

            if (Boolean.TRUE.equals(response.getErrors())) {
                throw new RuntimeException("데이터 색인 실패 (" + collectionId + ")");
            }
        }
    }

    @Override
    public void deleteIndex(String collectionId, List<String> chunkIds) {
        if (chunkIds.isEmpty()) return;

        int batchSize = 500;
        for (int batchIndex = 0; batchIndex < chunkIds.size(); batchIndex += batchSize) {
            StringBuilder body = new StringBuilder();

            for (int i = batchIndex; i < Math.min(chunkIds.size(), batchIndex + batchSize); i++) {
                try {
                    DeleteIndexBulkRequest req = DeleteIndexBulkRequest.builder()
                            .delete(DeleteIndexBulkRequest.Delete.builder()
                                    .id(chunkIds.get(i))
                                    .build())
                            .build();
                    body.append(objectMapper.writeValueAsString(req)).append("\n");
                } catch (JsonProcessingException ignored) {
                }
            }

            DeleteIndexBulkResponse response = indexerWebClient.post()
                    .uri(indexerProperty.getUrl() + "/" + collectionId + "/_bulk")
                    .contentType(MediaType.APPLICATION_NDJSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body.toString())
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::isError, resp ->
                            resp.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                    .flatMap(err -> Mono.error(new RuntimeException("색인 삭제 실패 (" + collectionId + ")"))))
                    .bodyToMono(DeleteIndexBulkResponse.class)
                    .blockOptional()
                    .orElseThrow(() -> new RuntimeException("색인 삭제 실패 (" + collectionId + ")"));

            if (Boolean.TRUE.equals(response.getErrors())) {
                throw new RuntimeException("색인 삭제 실패 (" + collectionId + ")");
            }
        }
    }
}
