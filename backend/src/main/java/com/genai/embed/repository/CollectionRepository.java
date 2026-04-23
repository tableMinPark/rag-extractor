package com.genai.embed.repository;

import com.genai.embed.repository.entity.CollectionEntity;
import com.genai.embed.repository.entity.DocumentEntity;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository {

    Optional<CollectionEntity> findCollectionByCollectionId(String collectionId);

    List<DocumentEntity> convertVector(String collectionId, List<DocumentEntity> documentEntities);

    void createIndex(String collectionId, List<DocumentEntity> documentEntities);

    void deleteIndex(String collectionId, List<String> chunkIds);
}
