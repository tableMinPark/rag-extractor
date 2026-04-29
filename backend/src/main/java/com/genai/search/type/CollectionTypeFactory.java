package com.genai.search.type;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionTypeFactory {

    public List<CollectionType> getAll() {
        return List.of(CollectionType.ai(), CollectionType.myai());
    }
}
