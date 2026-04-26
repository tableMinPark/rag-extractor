# RAG 검색 서비스 설계 — rag-extractor

## 목표

rag-genAI의 검색 기능(키워드 검색, 벡터 검색)을 rag-extractor에 이식하여 독립적인 RAG 검색 API를 제공한다.

## 범위

- 키워드 검색 API (`POST /search/keyword`)
- 벡터 검색 API (`POST /search/vector`)
- 리랭킹은 이식만 하고 API로 노출하지 않음 (다른 기능에서 사용 예정)

## 패키지 구조

```
com.genai.search
├── controller/
│   └── SearchController
├── service/
│   └── SearchService
├── repository/
│   ├── SearchRepository
│   └── impl/
│       └── SearchRepositoryImpl
│   ├── request/
│   │   ├── KeywordSearchRequest
│   │   └── VectorSearchRequest
│   ├── response/
│   │   └── SearchResponse
│   ├── wrapper/
│   │   └── Search
│   └── vo/
│       └── ConvertVectorVO
├── config/
│   └── properties/
│       └── SearchProperty
└── dto/
    ├── request/
    │   └── SearchRequestDto
    └── response/
        └── SearchResponseDto
```

## API 명세

### POST /search/keyword

키워드(BM25) 기반 검색.

**요청**
```json
{
  "query": "검색어",
  "collectionId": "gen_ai",
  "topK": 10,
  "aliases": []
}
```

- `query`: 필수. 검색 질의문
- `collectionId`: 필수. OpenSearch 인덱스 ID
- `topK`: 필수. 반환할 최대 문서 수
- `aliases`: 선택. 카테고리 코드 필터 목록. 비어있으면 전체 검색

**응답**
```json
[
  {
    "chunkId": 1,
    "passageId": 1,
    "sourceId": 1,
    "fileDetailId": 1,
    "name": "문서명",
    "title": "제목",
    "subTitle": "소제목",
    "thirdTitle": "3단계 제목",
    "compactContent": "압축 내용",
    "content": "본문",
    "subContent": "부가 내용",
    "context": "검색용 컨텍스트",
    "categoryCode": "카테고리 코드",
    "sourceType": "소스 타입",
    "score": 1.23,
    "sysCreateDt": "...",
    "sysModifyDt": "..."
  }
]
```

### POST /search/vector

임베딩 기반 시맨틱 검색. 요청/응답 구조는 키워드 검색과 동일.

내부적으로 임베딩 서버(`embed.url`)에 질의문을 벡터로 변환 후 OpenSearch kNN 쿼리를 수행.

## 이식 파일 목록

| 파일 | 출처 | 처리 방식 |
|---|---|---|
| `SearchRepository` | rag-genAI | 이식 (rerank 메서드 제외) |
| `SearchRepositoryImpl` | rag-genAI | 이식 (rerank 로직 내부 구현은 유지, API 노출 안 함) |
| `KeywordSearchRequest` | rag-genAI | 이식 |
| `VectorSearchRequest` | rag-genAI | 이식 |
| `SearchResponse` | rag-genAI | 이식 |
| `Search` wrapper | rag-genAI | 이식 |
| `ConvertVectorVO` | rag-genAI | 이식 |
| `SearchProperty` | rag-genAI | 이식 |
| `EmbedProperty` | extractor | 재사용 (이미 존재) |
| `SearchErrorException` | extractor | 재사용 (이미 존재) |
| `CollectionType` | 신규 | extractor용으로 새로 작성 |

## 데이터 흐름

### 키워드 검색
```
Client → SearchController → SearchService → SearchRepositoryImpl
       → OpenSearch (_search with multi_match query)
       → SearchResponse 파싱 → SearchResponseDto 변환 → Client
```

### 벡터 검색
```
Client → SearchController → SearchService → SearchRepositoryImpl
       → EmbedServer (POST /embed, 질의문 → 벡터)
       → OpenSearch (_search with kNN query)
       → SearchResponse 파싱 → SearchResponseDto 변환 → Client
```

## 설정 (application.yml 추가 항목)

```yaml
search:
  url: http://localhost:9200  # OpenSearch URL
```

`embed.url`은 기존 설정 재사용.

## 재사용 가능한 기존 extractor 컴포넌트

- `EmbedProperty` — 임베딩 서버 URL
- `SearchErrorException` — 검색 오류 예외
- `global.dto.ResponseDto` — 공통 응답 래퍼
- `global.enums.Response` — 응답 코드/메시지 (새 항목 추가 필요)
- `embed.config.WebClientConfig` — searchWebClient, rerankerWebClient Bean 추가 필요

## 주요 결정 사항

- 리랭킹: `SearchRepository`에 `rerank` 메서드 이식, `SearchRepositoryImpl`에 구현 유지. 단, `SearchController`/`SearchService`에서는 노출하지 않음.
- CollectionType: rag-genAI의 enum 방식 대신 동적으로 `collectionId`를 요청에서 받아 처리. `keywordSearchFields`와 `vectorSearchFields`는 `CollectionType` 빌더 패턴으로 요청 시 구성.
- aliases 필터: 빈 리스트이면 OpenSearch 쿼리에 filter 절 생략.
