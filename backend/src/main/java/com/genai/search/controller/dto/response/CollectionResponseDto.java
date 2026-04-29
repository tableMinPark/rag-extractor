package com.genai.search.controller.dto.response;

import com.genai.search.type.CollectionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CollectionResponseDto {

    private String collectionId;
    private String collectionName;

    public static CollectionResponseDto from(CollectionType collectionType) {
        return CollectionResponseDto.builder()
                .collectionId(collectionType.getCollectionId())
                .collectionName(collectionType.getCollectionName())
                .build();
    }

    public static List<CollectionResponseDto> fromList(List<CollectionType> collectionTypes) {
        return collectionTypes.stream().map(CollectionResponseDto::from).toList();
    }
}
