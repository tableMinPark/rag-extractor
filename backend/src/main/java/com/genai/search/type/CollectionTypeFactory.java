package com.genai.search.type;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionTypeFactory {

    private static final List<CollectionType> ALL = List.of(CollectionType.ai(), CollectionType.myai());

    public List<CollectionType> getAll() {
        return ALL;
    }
}
