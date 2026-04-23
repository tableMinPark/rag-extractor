package com.genai.embed.repository.entity;

import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntity {

    private String collectionId;
    private int numOfShards;
    private int numOfReplication;
    private List<String> fields;
}
