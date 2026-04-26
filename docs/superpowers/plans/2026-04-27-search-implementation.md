# RAG 검색 서비스 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** rag-genAI의 키워드/벡터 검색 기능을 rag-extractor에 이식하여 `POST /search/keyword`, `POST /search/vector` API를 제공한다.

**Architecture:** `com.genai.search` 패키지를 신규 생성하고, rag-genAI의 `SearchRepository`, `SearchRepositoryImpl`, 관련 request/response/wrapper/vo 파일을 패키지 경로만 변경하여 이식한다. `SearchController` → `SearchService` → `SearchRepositoryImpl` → OpenSearch/EmbedServer 순으로 호출된다.

**Tech Stack:** Spring Boot, WebFlux WebClient, OpenSearch (BM25 + kNN), FastAPI 임베딩 서버

---

## 파일 구조

### 신규 생성 파일

```
backend/src/main/java/com/genai/search/
├── controller/
│   └── SearchController.java
├── service/
│   └── SearchService.java
├── repository/
│   ├── SearchRepository.java
│   └── impl/
│       └── SearchRepositoryImpl.java
│   ├── entity/
│   │   └── DocumentEntity.java          (rag-genAI에서 이식)
│   ├── request/
│   │   ├── KeywordSearchRequest.java    (rag-genAI에서 이식)
│   │   ├── VectorSearchRequest.java     (rag-genAI에서 이식)
│   │   ├── RerankRequest.java           (rag-genAI에서 이식, 추후 활용)
│   ├── response/
│   │   ├── SearchResponse.java          (rag-genAI에서 이식)
│   │   └── RerankResponse.java          (rag-genAI에서 이식, 추후 활용)
│   ├── wrapper/
│   │   ├── Search.java                  (rag-genAI에서 이식)
│   │   └── Rerank.java                  (rag-genAI에서 이식, 추후 활용)
│   └── vo/
│       └── ConvertVectorVO.java         (rag-genAI에서 이식)
├── config/
│   ├── SearchWebClientConfig.java       (searchWebClient, rerankerWebClient Bean)
│   └── properties/
│       ├── SearchProperty.java          (rag-genAI에서 이식)
│       └── RerankerProperty.java        (rag-genAI에서 이식, 추후 활용)
└── dto/
    ├── request/
    │   └── SearchRequestDto.java
    └── response/
        └── SearchResponseDto.java
```

### 기존 수정 파일

```
backend/src/main/java/com/genai/global/enums/Response.java   (검색 응답 코드 추가)
backend/src/main/resources/application-local.yml             (engine.search, engine.reranker 설정 추가)
backend/src/main/resources/application-dev.yml              (engine.search, engine.reranker 설정 추가)
backend/src/main/resources/application-prd.yml              (engine.search, engine.reranker 설정 추가)
```

---

## Task 1: 설정 및 프로퍼티 파일 추가

**Files:**
- Create: `backend/src/main/java/com/genai/search/config/properties/SearchProperty.java`
- Create: `backend/src/main/java/com/genai/search/config/properties/RerankerProperty.java`
- Create: `backend/src/main/java/com/genai/search/config/SearchWebClientConfig.java`
- Modify: `backend/src/main/resources/application-local.yml`
- Modify: `backend/src/main/resources/application-dev.yml`
- Modify: `backend/src/main/resources/application-prd.yml`

- [ ] **Step 1: SearchProperty 생성**

`backend/src/main/java/com/genai/search/config/properties/SearchProperty.java`

```java
package com.genai.search.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "engine.search")
public class SearchProperty {

    private int connectTimeout;
    private int responseTimeout;
    private int readTimeout;
    private int writeTimeout;
    private String host;
    private int port;

    public String getUrl() {
        StringBuilder url = new StringBuilder();
        if (!host.startsWith("http")) url.append("http://");
        url.append(host).append(":").append(port);
        return url.toString().trim();
    }
}
```

- [ ] **Step 2: RerankerProperty 생성**

`backend/src/main/java/com/genai/search/config/properties/RerankerProperty.java`

```java
package com.genai.search.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "engine.reranker")
public class RerankerProperty {

    private int connectTimeout;
    private int responseTimeout;
    private int readTimeout;
    private int writeTimeout;
    private String host;
    private int port;
    private String path;

    public String getUrl() {
        StringBuilder url = new StringBuilder();
        if (!host.startsWith("http")) url.append("http://");
        url.append(host).append(":").append(port);
        url.append(path.startsWith("/") ? "" : "/").append(path);
        return url.toString().trim();
    }
}
```

- [ ] **Step 3: SearchWebClientConfig 생성**

`backend/src/main/java/com/genai/search/config/SearchWebClientConfig.java`

```java
package com.genai.search.config;

import com.genai.search.config.properties.RerankerProperty;
import com.genai.search.config.properties.SearchProperty;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration("searchWebClientConfig")
public class SearchWebClientConfig {

    @Bean(name = "searchWebClient")
    public WebClient searchWebClient(SearchProperty property) {
        return buildWebClient(property.getConnectTimeout(), property.getResponseTimeout(),
                property.getReadTimeout(), property.getWriteTimeout());
    }

    @Bean(name = "rerankerWebClient")
    public WebClient rerankerWebClient(RerankerProperty property) {
        return buildWebClient(property.getConnectTimeout(), property.getResponseTimeout(),
                property.getReadTimeout(), property.getWriteTimeout());
    }

    private WebClient buildWebClient(int connectTimeout, int responseTimeout, int readTimeout, int writeTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(responseTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                        .build())
                .build();
    }
}
```

- [ ] **Step 4: application-local.yml에 engine.search, engine.reranker 설정 추가**

`application-local.yml`의 `engine:` 블록 하위에 다음을 추가:

```yaml
  search:
    connect-timeout: 5000
    response-timeout: 300000
    read-timeout: 300000
    write-timeout: 300000
    host: localhost
    port: 9200
  reranker:
    connect-timeout: 5000
    response-timeout: 300000
    read-timeout: 300000
    write-timeout: 300000
    host: localhost
    port: 8001
    path: /rerank
```

- [ ] **Step 5: application-dev.yml, application-prd.yml 동일하게 설정 추가**

각 yml 파일의 `engine:` 블록 하위에 Step 4와 동일한 내용 추가 (host/port 값은 환경에 맞게).

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main/java/com/genai/search/config/ \
        backend/src/main/resources/application-local.yml \
        backend/src/main/resources/application-dev.yml \
        backend/src/main/resources/application-prd.yml
git commit -m "feat: 검색 설정 프로퍼티 및 WebClient Bean 추가"
```

---

## Task 2: repository 계층 이식

**Files:**
- Create: `backend/src/main/java/com/genai/search/repository/entity/DocumentEntity.java`
- Create: `backend/src/main/java/com/genai/search/repository/request/KeywordSearchRequest.java`
- Create: `backend/src/main/java/com/genai/search/repository/request/VectorSearchRequest.java`
- Create: `backend/src/main/java/com/genai/search/repository/request/RerankRequest.java`
- Create: `backend/src/main/java/com/genai/search/repository/response/SearchResponse.java`
- Create: `backend/src/main/java/com/genai/search/repository/response/RerankResponse.java`
- Create: `backend/src/main/java/com/genai/search/repository/wrapper/Search.java`
- Create: `backend/src/main/java/com/genai/search/repository/wrapper/Rerank.java`
- Create: `backend/src/main/java/com/genai/search/repository/vo/ConvertVectorVO.java`
- Create: `backend/src/main/java/com/genai/search/repository/SearchRepository.java`
- Create: `backend/src/main/java/com/genai/search/repository/impl/SearchRepositoryImpl.java`

- [ ] **Step 1: DocumentEntity 생성**

`backend/src/main/java/com/genai/search/repository/entity/DocumentEntity.java`

```java
package com.genai.search.repository.entity;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentEntity {

    @JsonAlias("chunk_id") @JsonProperty("chunk_id")
    private Long chunkId;

    @JsonAlias("passage_id") @JsonProperty("passage_id")
    private Long passageId;

    @JsonAlias("source_id") @JsonProperty("source_id")
    private Long sourceId;

    @JsonAlias("file_detail_id") @JsonProperty("file_detail_id")
    private Long fileDetailId;

    @JsonAlias("origin_file_name") @JsonProperty("origin_file_name")
    private String originFileName;

    @JsonAlias("name") @JsonProperty("name")
    private String name;

    @JsonAlias("title") @JsonProperty("title")
    private String title;

    @JsonAlias("sub_title") @JsonProperty("sub_title")
    private String subTitle;

    @JsonAlias("third_title") @JsonProperty("third_title")
    private String thirdTitle;

    @JsonAlias("compact_content") @JsonProperty("compact_content")
    private String compactContent;

    @JsonAlias("content") @JsonProperty("content")
    private String content;

    @JsonAlias("sub_content") @JsonProperty("sub_content")
    private String subContent;

    @JsonAlias("context") @JsonProperty("context")
    private String context;

    @Builder.Default
    @JsonAlias("vector-context") @JsonProperty("vector-context")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Float> contextVector = new ArrayList<>();

    @JsonAlias("url") @JsonProperty("url")
    private String url;

    @JsonAlias("category_code") @JsonProperty("category_code")
    private String categoryCode;

    @JsonAlias("source_type") @JsonProperty("source_type")
    private String sourceType;

    @JsonAlias("ext") @JsonProperty("ext")
    private String ext;

    @JsonAlias("sys_create_dt") @JsonProperty("sys_create_dt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sysCreateDt;

    @JsonAlias("sys_modify_dt") @JsonProperty("sys_modify_dt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sysModifyDt;

    @JsonAlias("alias") @JsonProperty("alias")
    private String alias;
}
```

- [ ] **Step 2: KeywordSearchRequest 생성**

`backend/src/main/java/com/genai/search/repository/request/KeywordSearchRequest.java`

```java
package com.genai.search.repository.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class KeywordSearchRequest {

    private final int size;
    private final List<Map<SortField, Order>> sort;
    private final Query query;

    public static Map<SortField, Order> sort(SortField sortField, String direction) {
        return Map.of(sortField, Order.builder().order(direction).build());
    }

    public static Query query(QueryType queryType, String query, List<String> fields, List<String> aliases) {
        Query.Bool.Must.MultiMatch multiMatch = Query.Bool.Must.MultiMatch.builder()
                .type(queryType.name()).query(query).fields(fields).build();

        Query.Bool.Must must = Query.Bool.Must.builder().multiMatch(multiMatch).build();

        List<Query.Bool.Filter> filter = aliases.stream()
                .map(alias -> Query.Bool.Filter.builder()
                        .term(Query.Bool.Filter.Term.builder().alias(alias).build())
                        .build())
                .toList();

        Query.Bool bool = Query.Bool.builder().must(List.of(must)).filter(filter).build();
        return Query.builder().bool(bool).build();
    }

    @Builder public record Order(String order) {}

    @Builder
    public record Query(Bool bool) {
        @Builder
        public record Bool(List<Must> must, List<Filter> filter) {
            @Builder
            public record Must(@JsonProperty("multi_match") MultiMatch multiMatch) {
                @Builder
                public record MultiMatch(String query, List<String> fields, String type) {}
            }
            @Builder
            public record Filter(Term term) {
                @Builder
                public record Term(String alias) {}
            }
        }
    }

    public enum SortField { _score }
    public enum QueryType { best_fields }
}
```

- [ ] **Step 3: VectorSearchRequest 생성**

`backend/src/main/java/com/genai/search/repository/request/VectorSearchRequest.java`

```java
package com.genai.search.repository.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class VectorSearchRequest {

    private final int size;
    private final Query query;

    public static Query query(List<String> fields, List<Float> vector, List<String> aliases) {
        Map<String, Field> knn = new HashMap<>();
        fields.forEach(field -> knn.put(field, Field.builder().k(20).vector(vector).build()));

        Query.Bool.Must must = Query.Bool.Must.builder().knn(knn).build();

        List<Query.Bool.Filter> filter = aliases.stream()
                .map(alias -> Query.Bool.Filter.builder()
                        .term(Query.Bool.Filter.Term.builder().alias(alias).build())
                        .build())
                .toList();

        Query.Bool bool = Query.Bool.builder().must(List.of(must)).filter(filter).build();
        return Query.builder().bool(bool).build();
    }

    @Builder
    public record Query(Bool bool) {
        @Builder
        public record Bool(List<Must> must, List<Filter> filter) {
            @Builder
            public record Must(Map<String, Field> knn) {}
            @Builder
            public record Filter(Term term) {
                @Builder
                public record Term(String alias) {}
            }
        }
    }

    @Builder
    public record Field(int k, List<Float> vector) {}
}
```

- [ ] **Step 4: RerankRequest 생성**

`backend/src/main/java/com/genai/search/repository/request/RerankRequest.java`

```java
package com.genai.search.repository.request;

import lombok.*;

import java.util.List;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class RerankRequest {

    private final String query;
    private final List<Document> documents;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private String id;
        private String content;
    }
}
```

- [ ] **Step 5: SearchResponse 생성**

`backend/src/main/java/com/genai/search/repository/response/SearchResponse.java`

```java
package com.genai.search.repository.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.wrapper.Search;
import lombok.*;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T extends DocumentEntity> {

    private Integer took;

    @JsonProperty("timed_out")
    private Boolean timeout;

    @JsonProperty("hits")
    private Result<T> result;

    public record Result<T extends DocumentEntity>(
        Total total,
        @JsonProperty("max_score") float maxScore,
        List<Search<T>> hits
    ) {
        public record Total(int value, String relation) {}
    }
}
```

- [ ] **Step 6: RerankResponse 생성**

`backend/src/main/java/com/genai/search/repository/response/RerankResponse.java`

```java
package com.genai.search.repository.response;

import lombok.*;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RerankResponse {
    private List<Document> documents;
    public record Document(String id, String content, float score) {}
}
```

- [ ] **Step 7: Search wrapper 생성**

`backend/src/main/java/com/genai/search/repository/wrapper/Search.java`

```java
package com.genai.search.repository.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genai.search.repository.entity.DocumentEntity;
import lombok.*;

@ToString
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Search<T extends DocumentEntity> {

    @JsonProperty("_score")
    private double score;

    @JsonProperty("_source")
    private T fields;
}
```

- [ ] **Step 8: Rerank wrapper 생성**

`backend/src/main/java/com/genai/search/repository/wrapper/Rerank.java`

```java
package com.genai.search.repository.wrapper;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.genai.search.repository.entity.DocumentEntity;
import lombok.*;

@Builder
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Rerank {

    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double rerankScore;

    @JsonAlias("fields") @JsonProperty("fields")
    private DocumentEntity document;
}
```

- [ ] **Step 9: ConvertVectorVO 생성**

`backend/src/main/java/com/genai/search/repository/vo/ConvertVectorVO.java`

```java
package com.genai.search.repository.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConvertVectorVO {

    @JsonAlias("id") @JsonProperty("id")
    private Long id;

    @JsonAlias("content") @JsonProperty("content")
    private String content;

    @JsonAlias("vector") @JsonProperty("vector")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Float> vector;
}
```

- [ ] **Step 10: SearchRepository 인터페이스 생성**

`backend/src/main/java/com/genai/search/repository/SearchRepository.java`

```java
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
```

- [ ] **Step 11: SearchRepositoryImpl 생성**

`backend/src/main/java/com/genai/search/repository/impl/SearchRepositoryImpl.java`

```java
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
```

- [ ] **Step 12: 커밋**

```bash
git add backend/src/main/java/com/genai/search/repository/
git commit -m "feat: 검색 repository 계층 이식 (keyword/vector/rerank)"
```

---

## Task 3: DTO 및 Service 계층 구현

**Files:**
- Create: `backend/src/main/java/com/genai/search/dto/request/SearchRequestDto.java`
- Create: `backend/src/main/java/com/genai/search/dto/response/SearchResponseDto.java`
- Create: `backend/src/main/java/com/genai/search/service/SearchService.java`
- Modify: `backend/src/main/java/com/genai/global/enums/Response.java`

- [ ] **Step 1: SearchRequestDto 생성**

`backend/src/main/java/com/genai/search/dto/request/SearchRequestDto.java`

```java
package com.genai.search.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SearchRequestDto {

    @NotBlank
    private String query;

    @NotBlank
    private String collectionId;

    @NotNull
    @Positive
    private Integer topK;

    private List<String> aliases = List.of();
}
```

- [ ] **Step 2: SearchResponseDto 생성**

`backend/src/main/java/com/genai/search/dto/response/SearchResponseDto.java`

```java
package com.genai.search.dto.response;

import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.wrapper.Search;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SearchResponseDto {

    private Long chunkId;
    private Long passageId;
    private Long sourceId;
    private Long fileDetailId;
    private String originFileName;
    private String name;
    private String title;
    private String subTitle;
    private String thirdTitle;
    private String compactContent;
    private String content;
    private String subContent;
    private String context;
    private String url;
    private String categoryCode;
    private String sourceType;
    private String ext;
    private String alias;
    private LocalDateTime sysCreateDt;
    private LocalDateTime sysModifyDt;
    private double score;

    public static SearchResponseDto from(Search<DocumentEntity> search) {
        DocumentEntity doc = search.getFields();
        return SearchResponseDto.builder()
                .chunkId(doc.getChunkId())
                .passageId(doc.getPassageId())
                .sourceId(doc.getSourceId())
                .fileDetailId(doc.getFileDetailId())
                .originFileName(doc.getOriginFileName())
                .name(doc.getName())
                .title(doc.getTitle())
                .subTitle(doc.getSubTitle())
                .thirdTitle(doc.getThirdTitle())
                .compactContent(doc.getCompactContent())
                .content(doc.getContent())
                .subContent(doc.getSubContent())
                .context(doc.getContext())
                .url(doc.getUrl())
                .categoryCode(doc.getCategoryCode())
                .sourceType(doc.getSourceType())
                .ext(doc.getExt())
                .alias(doc.getAlias())
                .sysCreateDt(doc.getSysCreateDt())
                .sysModifyDt(doc.getSysModifyDt())
                .score(search.getScore())
                .build();
    }

    public static List<SearchResponseDto> fromList(List<Search<DocumentEntity>> searches) {
        return searches.stream().map(SearchResponseDto::from).toList();
    }
}
```

- [ ] **Step 3: Response enum에 검색 응답 코드 추가**

`backend/src/main/java/com/genai/global/enums/Response.java`의 기존 마지막 항목 아래에 추가:

```java
// 검색
KEYWORD_SEARCH_SUCCESS(HttpStatus.OK, 1700, "키워드 검색에 성공했습니다.", ""),
VECTOR_SEARCH_SUCCESS(HttpStatus.OK, 1701, "벡터 검색에 성공했습니다.", ""),
SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1702, "검색 중 오류가 발생했습니다.", ""),
```

- [ ] **Step 4: SearchService 생성**

`backend/src/main/java/com/genai/search/service/SearchService.java`

키워드 검색 필드는 `title`, `sub_title`, `third_title`, `content`이고, 벡터 검색 필드는 `vector-context`로 고정합니다 (rag-genAI와 동일).

```java
package com.genai.search.service;

import com.genai.search.dto.request.SearchRequestDto;
import com.genai.search.dto.response.SearchResponseDto;
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
```

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/genai/search/dto/ \
        backend/src/main/java/com/genai/search/service/ \
        backend/src/main/java/com/genai/global/enums/Response.java
git commit -m "feat: 검색 DTO, Service, 응답 코드 추가"
```

---

## Task 4: Controller 구현 및 빌드 검증

**Files:**
- Create: `backend/src/main/java/com/genai/search/controller/SearchController.java`

- [ ] **Step 1: SearchController 생성**

`backend/src/main/java/com/genai/search/controller/SearchController.java`

```java
package com.genai.search.controller;

import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import com.genai.search.dto.request.SearchRequestDto;
import com.genai.search.dto.response.SearchResponseDto;
import com.genai.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SearchController", description = "RAG 검색 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "키워드 검색")
    @PostMapping("/keyword")
    public ResponseEntity<ResponseDto<List<SearchResponseDto>>> keywordSearch(
            @Valid @RequestBody SearchRequestDto request) {
        List<SearchResponseDto> result = searchService.keywordSearch(request);
        return ResponseEntity.ok(Response.KEYWORD_SEARCH_SUCCESS.toResponseDto(result));
    }

    @Operation(summary = "벡터 검색")
    @PostMapping("/vector")
    public ResponseEntity<ResponseDto<List<SearchResponseDto>>> vectorSearch(
            @Valid @RequestBody SearchRequestDto request) {
        List<SearchResponseDto> result = searchService.vectorSearch(request);
        return ResponseEntity.ok(Response.VECTOR_SEARCH_SUCCESS.toResponseDto(result));
    }
}
```

- [ ] **Step 2: 빌드 확인**

프로젝트 루트에서:

```bash
cd backend && ./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

오류가 있으면 import 경로 및 타입 불일치를 수정한다.

- [ ] **Step 3: 커밋**

```bash
git add backend/src/main/java/com/genai/search/controller/
git commit -m "feat: 검색 컨트롤러 추가 (POST /search/keyword, POST /search/vector)"
```

---

## 셀프 리뷰 체크리스트

- [x] **스펙 커버리지:** `POST /search/keyword`, `POST /search/vector` 모두 구현됨. 리랭킹은 이식만 하고 API 노출 안 함.
- [x] **Placeholder 없음:** 모든 코드 블록에 실제 구현 포함.
- [x] **타입 일관성:**
  - `SearchRepository` 인터페이스의 반환 타입 `List<Search<DocumentEntity>>`와 `SearchRepositoryImpl` 구현 일치
  - `SearchResponseDto.from(Search<DocumentEntity>)` — `Search`의 `getFields()` 반환 타입 `DocumentEntity`와 일치
  - `SearchService`에서 `SearchRepository` 메서드 시그니처와 호출부 일치
- [x] **설정:** `engine.search.*`, `engine.reranker.*` 모두 application yml에 추가
- [x] **기존 재사용:** `EmbedProperty`, `SearchErrorException`은 기존 extractor 클래스 그대로 사용
