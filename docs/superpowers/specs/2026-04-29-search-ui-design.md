# RAG 검색 UI 통합 설계

## 목표

- 기존 `search/keyword/page.tsx`, `search/vector/page.tsx` 두 페이지를 `search/page.tsx` 하나로 통합
- 컬렉션 목록 API (`GET /collection`) 및 카테고리 목록 API (`GET /search/category`) 백엔드 구현
- 프론트엔드 실제 API 연동

## 백엔드 추가 구현

### 신규 파일

| 파일 | 역할 |
|---|---|
| `search/type/CollectionType.java` | collectionId, collectionName, keywordSearchFields, vectorSearchFields 보유 (mappingClass 제거) |
| `search/type/CollectionTypeFactory.java` | 전체 컬렉션 목록 반환 메서드 포함 |
| `search/dto/response/CollectionResponseDto.java` | collectionId, collectionName |
| `search/dto/response/CategoryResponseDto.java` | code, name |
| `search/controller/CollectionController.java` | GET /collection |

### 기존 수정 파일

| 파일 | 변경 내용 |
|---|---|
| `search/controller/SearchController.java` | GET /search/category 엔드포인트 추가 |
| `global/enums/Response.java` | GET_COLLECTIONS_SUCCESS(1703), GET_SEARCH_CATEGORIES_SUCCESS(1704) 추가 |

### API 명세

#### GET /collection
```json
[
  { "collectionId": "gen_ai", "collectionName": "AI" },
  { "collectionId": "gen_myai", "collectionName": "나만의 AI" }
]
```

#### GET /search/category
카테고리 그룹 코드 `"TRAIN"` 기준으로 CommonCodeService에서 조회.
```json
[
  { "code": "TRAIN-LAW", "name": "법령" },
  { "code": "TRAIN-GUIDE", "name": "지침" },
  { "code": "TRAIN-MANUAL", "name": "메뉴얼" },
  { "code": "TRAIN-EDU", "name": "교육 자료" }
]
```

### CollectionType 설계

rag-genAI의 CollectionType에서 `mappingClass` 제거. `collectionName` 추가.

```java
public class CollectionType {
    private final String collectionId;
    private final String collectionName;
    private final List<String> keywordSearchFields;
    private final List<String> vectorSearchFields;

    public static CollectionType ai() { ... "gen_ai", "AI" ... }
    public static CollectionType myai() { ... "gen_myai", "나만의 AI" ... }
}
```

### CollectionTypeFactory 설계

```java
@Component
public class CollectionTypeFactory {
    public List<CollectionType> getAll() {
        return List.of(CollectionType.ai(), CollectionType.myai());
    }
}
```

---

## 프론트엔드

### 신규/수정 파일

| 파일 | 내용 |
|---|---|
| `api/search.ts` | 신규 — keywordSearchApi, vectorSearchApi, getCollectionsApi, getSearchCategoriesApi |
| `app/(main)/search/page.tsx` | 통합 검색 페이지로 교체 |
| `app/(main)/search/keyword/page.tsx` | 삭제 |
| `app/(main)/search/vector/page.tsx` | 삭제 |
| `public/const/menu.ts` | search path를 `/search/keyword` → `/search`로 변경 |

### UI 구조

```
[검색 설정 패널]
  - 컬렉션 선택: 드롭다운 (GET /collection, 첫 번째 항목 기본값)
  - 카테고리 선택: 다중 체크박스 (GET /search/category, 전체 선택 기본값)
  - 검색 모드: 라디오 버튼 3개
      ○ 키워드 검색
      ○ 벡터 검색
      ○ 키워드 + 벡터 (하이브리드)
  - 리랭킹: 체크박스 (선택 시 검색 결과를 리랭커 API로 재정렬)

[검색 바]
  - 텍스트 입력 + 검색 버튼

[검색 결과]
  - 결과 카드 목록 (score, title, subTitle, content, subContent)
  - 페이지네이션
  - 상세 모달
```

### 검색 모드별 동작

| 모드 | 동작 |
|---|---|
| 키워드 | POST /search/keyword 단독 호출 |
| 벡터 | POST /search/vector 단독 호출 |
| 키워드 + 벡터 | 두 API 병렬 호출 → 결과 합산 → chunkId 기준 중복 제거 |

### 리랭킹

- 체크박스 선택 시 검색 결과를 POST /search/rerank (추후 구현 예정)로 재정렬
- **현재 단계에서는 UI만 구성, 실제 리랭킹 API 호출은 미구현 (TODO 표시)**

### SearchRequestDto (프론트→백)

```ts
{
  query: string
  collectionId: string
  topK: number         // 고정 10
  aliases: string[]    // 선택된 카테고리 code 목록 (전체 선택 시 빈 배열)
}
```

### SearchResponseDto (백→프론트) 매핑

```ts
interface SearchResultItem {
  chunkId: number
  sourceId: number
  score: number
  title: string
  subTitle: string
  thirdTitle: string
  content: string
  subContent: string
  context: string
  categoryCode: string
  name: string
  sysCreateDt: string
}
```

### 카테고리 선택 → aliases 변환

- 전체 선택 상태: `aliases = []` (빈 배열, 필터 없이 전체 검색)
- 일부 선택: 선택된 code 배열 전달

## 라우팅 변경

- `menu.ts`의 search path: `/search/keyword` → `/search`
- `search/keyword`, `search/vector` 하위 라우트 삭제
