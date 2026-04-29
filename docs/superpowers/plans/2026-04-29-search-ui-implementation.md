# RAG 검색 UI 통합 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 백엔드에 컬렉션/카테고리 조회 API를 추가하고, 분산된 두 검색 페이지를 단일 `search/page.tsx`로 통합하여 키워드·벡터·하이브리드 검색과 리랭킹 UI를 제공한다.

**Architecture:** 백엔드는 `CollectionType`/`CollectionTypeFactory`를 이식해 `GET /collection`을 제공하고, 기존 `CommonCodeService`를 재사용해 `GET /search/category`를 추가한다. 프론트엔드는 `api/search.ts`에 모든 검색 관련 API 함수를 모으고, `search/page.tsx` 단일 파일이 라디오(3모드) + 리랭킹 체크박스 + 컬렉션/카테고리 선택을 포함하는 통합 UI를 렌더링한다.

**Tech Stack:** Spring Boot (백엔드), Next.js 15 App Router + TypeScript + Tailwind CSS (프론트엔드), Axios

---

## 파일 구조

### 백엔드 신규 생성
```
backend/src/main/java/com/genai/search/type/CollectionType.java
backend/src/main/java/com/genai/search/type/CollectionTypeFactory.java
backend/src/main/java/com/genai/search/dto/response/CollectionResponseDto.java
backend/src/main/java/com/genai/search/dto/response/CategoryResponseDto.java
backend/src/main/java/com/genai/search/controller/CollectionController.java
```

### 백엔드 수정
```
backend/src/main/java/com/genai/search/controller/SearchController.java   (GET /search/category 추가)
backend/src/main/java/com/genai/global/enums/Response.java                 (1703, 1704 추가)
```

### 프론트엔드 신규 생성
```
frontend/api/search.ts
```

### 프론트엔드 수정
```
frontend/app/(main)/search/page.tsx          (전면 교체)
frontend/public/const/menu.ts               (path 수정)
```

### 프론트엔드 삭제
```
frontend/app/(main)/search/keyword/page.tsx
frontend/app/(main)/search/vector/page.tsx
```

---

## Task 1: 백엔드 — CollectionType, CollectionTypeFactory, CollectionController

**Files:**
- Create: `backend/src/main/java/com/genai/search/type/CollectionType.java`
- Create: `backend/src/main/java/com/genai/search/type/CollectionTypeFactory.java`
- Create: `backend/src/main/java/com/genai/search/dto/response/CollectionResponseDto.java`
- Create: `backend/src/main/java/com/genai/search/controller/CollectionController.java`
- Modify: `backend/src/main/java/com/genai/global/enums/Response.java`

- [ ] **Step 1: CollectionType 생성**

`backend/src/main/java/com/genai/search/type/CollectionType.java`

```java
package com.genai.search.type;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CollectionType {

    private final String collectionId;
    private final String collectionName;
    private final List<String> keywordSearchFields;
    private final List<String> vectorSearchFields;

    @Builder
    private CollectionType(String collectionId, String collectionName,
                           List<String> keywordSearchFields, List<String> vectorSearchFields) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.keywordSearchFields = keywordSearchFields;
        this.vectorSearchFields = vectorSearchFields;
    }

    public static CollectionType ai() {
        return CollectionType.builder()
                .collectionId("gen_ai")
                .collectionName("AI")
                .keywordSearchFields(List.of("title", "sub_title", "third_title", "content"))
                .vectorSearchFields(List.of("vector-context"))
                .build();
    }

    public static CollectionType myai() {
        return CollectionType.builder()
                .collectionId("gen_myai")
                .collectionName("나만의 AI")
                .keywordSearchFields(List.of("title", "sub_title", "third_title", "content"))
                .vectorSearchFields(List.of("vector-context"))
                .build();
    }
}
```

- [ ] **Step 2: CollectionTypeFactory 생성**

`backend/src/main/java/com/genai/search/type/CollectionTypeFactory.java`

```java
package com.genai.search.type;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionTypeFactory {

    public List<CollectionType> getAll() {
        return List.of(CollectionType.ai(), CollectionType.myai());
    }
}
```

- [ ] **Step 3: CollectionResponseDto 생성**

`backend/src/main/java/com/genai/search/dto/response/CollectionResponseDto.java`

```java
package com.genai.search.dto.response;

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
```

- [ ] **Step 4: Response enum에 1703 추가**

`backend/src/main/java/com/genai/global/enums/Response.java`의 `SEARCH_ERROR` 아래에 추가:

```java
GET_COLLECTIONS_SUCCESS(HttpStatus.OK, 1703, "컬렉션 목록 조회에 성공했습니다.", ""),
```

- [ ] **Step 5: CollectionController 생성**

`backend/src/main/java/com/genai/search/controller/CollectionController.java`

```java
package com.genai.search.controller;

import com.genai.global.dto.ResponseDto;
import com.genai.global.enums.Response;
import com.genai.search.dto.response.CollectionResponseDto;
import com.genai.search.type.CollectionTypeFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CollectionController", description = "컬렉션 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/collection")
public class CollectionController {

    private final CollectionTypeFactory collectionTypeFactory;

    @Operation(summary = "컬렉션 목록 조회")
    @GetMapping
    public ResponseEntity<ResponseDto<List<CollectionResponseDto>>> getCollections() {
        List<CollectionResponseDto> result = CollectionResponseDto.fromList(collectionTypeFactory.getAll());
        return ResponseEntity.ok(Response.GET_COLLECTIONS_SUCCESS.toResponseDto(result));
    }
}
```

- [ ] **Step 6: 빌드 확인**

```bash
cd /Users/tableminpark/IdeaProjects/rag-extractor/backend && ./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: 커밋**

```bash
git add backend/src/main/java/com/genai/search/type/ \
        backend/src/main/java/com/genai/search/dto/response/CollectionResponseDto.java \
        backend/src/main/java/com/genai/search/controller/CollectionController.java \
        backend/src/main/java/com/genai/global/enums/Response.java
git commit -m "feat: 컬렉션 목록 조회 API 추가 (GET /collection)"
```

---

## Task 2: 백엔드 — GET /search/category 엔드포인트 추가

**Files:**
- Create: `backend/src/main/java/com/genai/search/dto/response/CategoryResponseDto.java`
- Modify: `backend/src/main/java/com/genai/search/controller/SearchController.java`
- Modify: `backend/src/main/java/com/genai/global/enums/Response.java`

- [ ] **Step 1: CategoryResponseDto 생성**

`backend/src/main/java/com/genai/search/dto/response/CategoryResponseDto.java`

```java
package com.genai.search.dto.response;

import com.genai.common.vo.CommonCodeVO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryResponseDto {

    private String code;
    private String name;

    public static CategoryResponseDto from(CommonCodeVO vo) {
        return CategoryResponseDto.builder()
                .code(vo.getCode())
                .name(vo.getCodeName())
                .build();
    }

    public static List<CategoryResponseDto> fromList(List<CommonCodeVO> vos) {
        return vos.stream().map(CategoryResponseDto::from).toList();
    }
}
```

- [ ] **Step 2: Response enum에 1704 추가**

`backend/src/main/java/com/genai/global/enums/Response.java`의 `GET_COLLECTIONS_SUCCESS` 아래에 추가:

```java
GET_SEARCH_CATEGORIES_SUCCESS(HttpStatus.OK, 1704, "검색 카테고리 목록 조회에 성공했습니다.", ""),
```

- [ ] **Step 3: SearchController에 GET /search/category 추가**

`backend/src/main/java/com/genai/search/controller/SearchController.java`를 읽고, 기존 import에 다음을 추가:

```java
import com.genai.common.service.CommonCodeService;
import com.genai.common.vo.CommonCodeVO;
import com.genai.search.dto.response.CategoryResponseDto;
```

클래스 필드에 추가:

```java
private final CommonCodeService commonCodeService;
```

`@RequiredArgsConstructor`가 이미 있으므로 생성자는 자동 주입됨.

엔드포인트 추가:

```java
private static final String CATEGORY_CODE_GROUP = "TRAIN";

@Operation(summary = "검색 카테고리 목록 조회")
@GetMapping("/category")
public ResponseEntity<ResponseDto<List<CategoryResponseDto>>> getCategories() {
    List<CommonCodeVO> vos = commonCodeService.getCommonCodes(CATEGORY_CODE_GROUP);
    List<CategoryResponseDto> result = CategoryResponseDto.fromList(vos);
    return ResponseEntity.ok(Response.GET_SEARCH_CATEGORIES_SUCCESS.toResponseDto(result));
}
```

- [ ] **Step 4: 빌드 확인**

```bash
cd /Users/tableminpark/IdeaProjects/rag-extractor/backend && ./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/genai/search/dto/response/CategoryResponseDto.java \
        backend/src/main/java/com/genai/search/controller/SearchController.java \
        backend/src/main/java/com/genai/global/enums/Response.java
git commit -m "feat: 검색 카테고리 목록 조회 API 추가 (GET /search/category)"
```

---

## Task 3: 프론트엔드 — api/search.ts 생성

**Files:**
- Create: `frontend/api/search.ts`

- [ ] **Step 1: api/search.ts 생성**

`frontend/api/search.ts`

```typescript
import { client } from './client'
import { ApiResponse } from '@/types/api'

// ─── 타입 정의 ────────────────────────────────────────────

export interface SearchCollection {
  collectionId: string
  collectionName: string
}

export interface SearchCategory {
  code: string
  name: string
}

export interface SearchRequestBody {
  query: string
  collectionId: string
  topK: number
  aliases: string[]
}

export interface SearchResultItem {
  chunkId: number
  passageId: number
  sourceId: number
  fileDetailId: number
  originFileName: string
  name: string
  title: string
  subTitle: string
  thirdTitle: string
  compactContent: string
  content: string
  subContent: string
  context: string
  url: string
  categoryCode: string
  sourceType: string
  ext: string
  alias: string
  sysCreateDt: string
  sysModifyDt: string
  score: number
}

// ─── API 함수 ────────────────────────────────────────────

export const getCollectionsApi = async (): Promise<ApiResponse<SearchCollection[]>> => {
  const response = await client.get<ApiResponse<SearchCollection[]>>('/collection')
  return response.data
}

export const getSearchCategoriesApi = async (): Promise<ApiResponse<SearchCategory[]>> => {
  const response = await client.get<ApiResponse<SearchCategory[]>>('/search/category')
  return response.data
}

export const keywordSearchApi = async (
  body: SearchRequestBody,
): Promise<ApiResponse<SearchResultItem[]>> => {
  const response = await client.post<ApiResponse<SearchResultItem[]>>('/search/keyword', body)
  return response.data
}

export const vectorSearchApi = async (
  body: SearchRequestBody,
): Promise<ApiResponse<SearchResultItem[]>> => {
  const response = await client.post<ApiResponse<SearchResultItem[]>>('/search/vector', body)
  return response.data
}
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/api/search.ts
git commit -m "feat: 검색 API 함수 추가 (search.ts)"
```

---

## Task 4: 프론트엔드 — search/page.tsx 통합 검색 페이지 구현

**Files:**
- Modify: `frontend/app/(main)/search/page.tsx`   (전면 교체)
- Modify: `frontend/public/const/menu.ts`
- Delete: `frontend/app/(main)/search/keyword/page.tsx`
- Delete: `frontend/app/(main)/search/vector/page.tsx`

- [ ] **Step 1: menu.ts search path 수정**

`frontend/public/const/menu.ts`에서:

```typescript
// 변경 전
search: {
  ...
  path: '/search/keyword',
  ...
},

// 변경 후
search: {
  ...
  path: '/search',
  ...
},
```

- [ ] **Step 2: search/page.tsx 전면 교체**

`frontend/app/(main)/search/page.tsx`를 아래 내용으로 교체:

```tsx
'use client'

import React, { useCallback, useEffect, useState } from 'react'
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  Layers,
  LayoutTemplate,
  Loader2,
  Maximize2,
  Search,
  SearchCode,
  X,
} from 'lucide-react'
import {
  getCollectionsApi,
  getSearchCategoriesApi,
  keywordSearchApi,
  vectorSearchApi,
  SearchCollection,
  SearchCategory,
  SearchResultItem,
  SearchRequestBody,
} from '@/api/search'

// ─── 상수 ────────────────────────────────────────────────
type SearchMode = 'KEYWORD' | 'VECTOR' | 'HYBRID'
const TOP_K = 10
const SEARCH_MODE_OPTIONS: { value: SearchMode; label: string }[] = [
  { value: 'KEYWORD', label: '키워드 검색' },
  { value: 'VECTOR', label: '벡터 검색' },
  { value: 'HYBRID', label: '키워드 + 벡터 (하이브리드)' },
]

// ─── 유틸 ────────────────────────────────────────────────
function deduplicateByChunkId(items: SearchResultItem[]): SearchResultItem[] {
  const seen = new Set<number>()
  return items.filter((item) => {
    if (seen.has(item.chunkId)) return false
    seen.add(item.chunkId)
    return true
  })
}

// ─── 컴포넌트 ────────────────────────────────────────────
export default function SearchPage() {
  // 초기 데이터
  const [collections, setCollections] = useState<SearchCollection[]>([])
  const [categories, setCategories] = useState<SearchCategory[]>([])

  // 검색 설정
  const [collectionId, setCollectionId] = useState('')
  const [selectedCodes, setSelectedCodes] = useState<string[]>([])
  const [searchMode, setSearchMode] = useState<SearchMode>('KEYWORD')
  const [useRerank, setUseRerank] = useState(false)

  // 검색 상태
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<SearchResultItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)

  // 페이지네이션
  const [page, setPage] = useState(1)
  const ITEMS_PER_PAGE = 10
  const pagedResults = results.slice((page - 1) * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)
  const totalPages = Math.ceil(results.length / ITEMS_PER_PAGE)

  // 모달
  const [selectedItem, setSelectedItem] = useState<SearchResultItem | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  // ─── 초기 로드 ──────────────────────────────────────────
  useEffect(() => {
    const load = async () => {
      try {
        const [colRes, catRes] = await Promise.all([
          getCollectionsApi(),
          getSearchCategoriesApi(),
        ])
        setCollections(colRes.result)
        setCategories(catRes.result)
        if (colRes.result.length > 0) setCollectionId(colRes.result[0].collectionId)
        // 전체 카테고리 기본 선택
        setSelectedCodes(catRes.result.map((c) => c.code))
      } catch (err) {
        console.error(err)
      }
    }
    load()
  }, [])

  // ─── 카테고리 토글 ──────────────────────────────────────
  const toggleCategory = (code: string) => {
    setSelectedCodes((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code],
    )
  }

  const toggleAllCategories = () => {
    if (selectedCodes.length === categories.length) {
      setSelectedCodes([])
    } else {
      setSelectedCodes(categories.map((c) => c.code))
    }
  }

  // ─── 검색 실행 ──────────────────────────────────────────
  const handleSearch = useCallback(async () => {
    if (!query.trim()) {
      alert('검색어를 입력해주세요.')
      return
    }
    if (!collectionId) {
      alert('컬렉션을 선택해주세요.')
      return
    }

    setIsLoading(true)
    setHasSearched(true)
    setPage(1)

    const body: SearchRequestBody = {
      query,
      collectionId,
      topK: TOP_K,
      // 전체 선택이면 빈 배열(필터 없음), 일부 선택이면 해당 코드 목록
      aliases: selectedCodes.length === categories.length ? [] : selectedCodes,
    }

    try {
      let merged: SearchResultItem[] = []

      if (searchMode === 'KEYWORD') {
        const res = await keywordSearchApi(body)
        merged = res.result
      } else if (searchMode === 'VECTOR') {
        const res = await vectorSearchApi(body)
        merged = res.result
      } else {
        // HYBRID: 병렬 호출 후 합산, chunkId 기준 중복 제거
        const [kwRes, vecRes] = await Promise.all([
          keywordSearchApi(body),
          vectorSearchApi(body),
        ])
        merged = deduplicateByChunkId([...kwRes.result, ...vecRes.result])
      }

      // TODO: useRerank === true 일 때 /search/rerank 호출 (추후 구현)
      if (useRerank) {
        console.log('[리랭킹] 추후 구현 예정 - 현재는 원본 결과 표시')
      }

      setResults(merged)
    } catch (err) {
      console.error(err)
      setResults([])
    } finally {
      setIsLoading(false)
    }
  }, [query, collectionId, selectedCodes, categories.length, searchMode, useRerank])

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSearch()
  }

  return (
    <div className="flex h-full w-full flex-col gap-4 p-6">

      {/* ── 헤더 ── */}
      <div className="flex items-center gap-3">
        <SearchCode className="text-primary h-8 w-8" />
        <div>
          <h2 className="text-2xl font-bold text-gray-800">RAG 검색</h2>
          <p className="mt-1 text-xs text-gray-500">
            키워드·벡터·하이브리드 검색으로 청크를 탐색합니다.
          </p>
        </div>
      </div>

      {/* ── 검색 설정 패널 ── */}
      <div className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5 shadow-sm">

        {/* 컬렉션 + 검색 모드 */}
        <div className="flex flex-wrap items-center gap-6">
          {/* 컬렉션 */}
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-500">컬렉션</span>
            <select
              value={collectionId}
              onChange={(e) => setCollectionId(e.target.value)}
              className="focus:border-primary focus:ring-primary h-10 w-48 cursor-pointer appearance-none rounded-lg border border-gray-200 bg-gray-50 px-3 text-sm font-bold text-gray-700 outline-none focus:bg-white focus:ring-1"
            >
              {collections.map((col) => (
                <option key={col.collectionId} value={col.collectionId}>
                  {col.collectionName}
                </option>
              ))}
            </select>
          </div>

          {/* 검색 모드 라디오 */}
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-500">검색 모드</span>
            <div className="flex items-center gap-4">
              {SEARCH_MODE_OPTIONS.map((opt) => (
                <label key={opt.value} className="flex cursor-pointer items-center gap-1.5">
                  <input
                    type="radio"
                    name="searchMode"
                    value={opt.value}
                    checked={searchMode === opt.value}
                    onChange={() => setSearchMode(opt.value)}
                    className="text-primary accent-primary"
                  />
                  <span className="text-sm text-gray-700">{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* 리랭킹 */}
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-500">리랭킹</span>
            <label className="flex cursor-pointer items-center gap-1.5">
              <input
                type="checkbox"
                checked={useRerank}
                onChange={(e) => setUseRerank(e.target.checked)}
                className="text-primary accent-primary h-4 w-4"
              />
              <span className="text-sm text-gray-700">리랭킹 적용</span>
            </label>
          </div>
        </div>

        {/* 카테고리 다중 선택 */}
        <div className="flex flex-col gap-2">
          <div className="flex items-center gap-3">
            <span className="text-xs font-bold text-gray-500">카테고리</span>
            <button
              onClick={toggleAllCategories}
              className="text-primary text-xs font-bold hover:underline"
            >
              {selectedCodes.length === categories.length ? '전체 해제' : '전체 선택'}
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {categories.map((cat) => (
              <label
                key={cat.code}
                className={`flex cursor-pointer items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-bold transition-colors ${
                  selectedCodes.includes(cat.code)
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-gray-200 bg-gray-50 text-gray-500 hover:bg-gray-100'
                }`}
              >
                <input
                  type="checkbox"
                  checked={selectedCodes.includes(cat.code)}
                  onChange={() => toggleCategory(cat.code)}
                  className="hidden"
                />
                {cat.name}
              </label>
            ))}
          </div>
        </div>

        {/* 검색 바 */}
        <div className="flex gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="찾으시는 단어나 문장을 입력하세요..."
              className="focus:border-primary focus:ring-primary h-12 w-full rounded-lg border border-gray-200 bg-white pr-4 pl-11 text-sm outline-none placeholder:text-gray-400 focus:ring-1"
            />
            <Search className="absolute top-1/2 left-4 h-5 w-5 -translate-y-1/2 text-gray-400" />
          </div>
          <button
            onClick={handleSearch}
            disabled={isLoading}
            className="bg-primary hover:bg-primary/90 flex h-12 w-24 shrink-0 items-center justify-center rounded-lg text-sm font-bold text-white shadow-md transition-all active:scale-95 disabled:opacity-60"
          >
            {isLoading ? <Loader2 className="h-5 w-5 animate-spin" /> : '검색'}
          </button>
        </div>
      </div>

      {/* ── 결과 영역 ── */}
      <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        {/* 결과 헤더 */}
        <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-4">
          <span className="flex items-center gap-2 text-sm font-bold text-gray-700">
            <Layers className="h-4 w-4" />
            검색 결과
          </span>
          {hasSearched && (
            <span className="text-xs text-gray-500">
              총 <span className="text-primary font-bold">{results.length}</span>건
            </span>
          )}
        </div>

        {/* 리스트 */}
        <div className="scrollbar-thin scrollbar-thumb-gray-200 flex-1 overflow-y-auto bg-gray-50/30 p-6">
          {isLoading ? (
            <div className="flex h-full flex-col items-center justify-center gap-3 text-gray-400">
              <Loader2 className="text-primary h-8 w-8 animate-spin" />
              <p className="text-sm">검색 중...</p>
            </div>
          ) : !hasSearched ? (
            <div className="flex h-full flex-col items-center justify-center gap-4">
              <Search className="h-12 w-12 text-gray-200" />
              <p className="text-sm font-medium text-gray-400">
                검색어를 입력하여 문서를 탐색하세요.
              </p>
            </div>
          ) : pagedResults.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-4">
              <X className="h-12 w-12 text-gray-200" />
              <p className="text-sm font-medium text-gray-400">일치하는 결과가 없습니다.</p>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {pagedResults.map((item) => (
                <div
                  key={item.chunkId}
                  onClick={() => { setSelectedItem(item); setIsModalOpen(true) }}
                  className="group hover:border-primary/50 relative flex cursor-pointer flex-col gap-3 rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-all hover:shadow-md"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex min-w-0 flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="shrink-0 rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-bold text-gray-500">
                          {item.categoryCode}
                        </span>
                        <h4 className="group-hover:text-primary line-clamp-1 text-sm font-bold text-gray-900 transition-colors">
                          {item.title || item.name}
                        </h4>
                      </div>
                      {item.subTitle && (
                        <span className="text-xs text-gray-500">{item.subTitle}</span>
                      )}
                    </div>
                    <div
                      className={`flex shrink-0 items-center gap-1 rounded-md px-2 py-1 text-xs font-bold ${
                        item.score >= 0.8
                          ? 'border border-green-100 bg-green-50 text-green-700'
                          : item.score >= 0.5
                            ? 'border border-yellow-100 bg-yellow-50 text-yellow-700'
                            : 'border border-gray-100 bg-gray-50 text-gray-600'
                      }`}
                    >
                      <span className="mr-1 text-[10px] text-gray-400 uppercase">Score</span>
                      {typeof item.score === 'number' ? item.score.toFixed(4) : item.score}
                    </div>
                  </div>

                  {item.content && (
                    <div className="border-l-2 border-gray-100 pl-3">
                      <p className="line-clamp-2 text-xs leading-relaxed text-gray-700">
                        {item.content}
                      </p>
                    </div>
                  )}

                  {item.subContent && (
                    <div className="rounded bg-gray-50 p-2.5">
                      <p className="line-clamp-2 font-mono text-[11px] leading-relaxed text-gray-500">
                        {item.subContent}
                      </p>
                    </div>
                  )}

                  <div className="absolute right-5 bottom-5 opacity-0 transition-opacity group-hover:opacity-100">
                    <Maximize2 className="text-primary h-4 w-4" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 페이지네이션 */}
        {hasSearched && results.length > ITEMS_PER_PAGE && (
          <div className="flex items-center justify-between border-t border-gray-100 bg-white px-6 py-3">
            <span className="text-xs text-gray-500">
              Page <span className="font-bold text-gray-800">{page}</span> of {totalPages}
            </span>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
                className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const start = Math.max(1, Math.min(page - 2, totalPages - 4))
                const pNum = start + i
                if (pNum > totalPages) return null
                return (
                  <button
                    key={pNum}
                    onClick={() => setPage(pNum)}
                    className={`h-8 w-8 rounded-lg text-xs font-bold transition-colors ${
                      page === pNum ? 'bg-primary text-white' : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    {pNum}
                  </button>
                )
              })}
              <button
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* ── 상세 모달 ── */}
      {isModalOpen && selectedItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
          <div className="animate-in zoom-in-95 flex h-[80vh] w-full max-w-3xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl duration-200">
            <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
              <div className="flex items-center gap-3">
                <span className="rounded bg-gray-100 px-2 py-0.5 text-xs font-bold text-gray-600">
                  {selectedItem.categoryCode}
                </span>
                <h3 className="line-clamp-1 text-lg font-bold text-gray-800">
                  {selectedItem.title || selectedItem.name}
                </h3>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="rounded-full p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            <div className="scrollbar-thin scrollbar-thumb-gray-300 flex-1 overflow-y-auto p-6">
              <div className="flex flex-col gap-6">
                <div className="rounded-lg border border-gray-100 bg-gray-50 p-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">Score</span>
                      <span className="text-primary text-sm font-bold">
                        {typeof selectedItem.score === 'number'
                          ? selectedItem.score.toFixed(6)
                          : selectedItem.score}
                      </span>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">Chunk ID</span>
                      <span className="font-mono text-xs text-gray-600">{selectedItem.chunkId}</span>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">Source</span>
                      <span className="text-xs text-gray-600">{selectedItem.name}</span>
                    </div>
                    {selectedItem.subTitle && (
                      <div className="col-span-2">
                        <span className="mb-1 block text-xs font-bold text-gray-400">Sub Title</span>
                        <span className="text-sm text-gray-700">{selectedItem.subTitle}</span>
                      </div>
                    )}
                    {selectedItem.thirdTitle && (
                      <div className="col-span-2">
                        <span className="mb-1 block text-xs font-bold text-gray-400">Third Title</span>
                        <span className="text-sm text-gray-700">{selectedItem.thirdTitle}</span>
                      </div>
                    )}
                  </div>
                </div>

                <div>
                  <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                    <FileText className="text-primary h-4 w-4" /> Content
                  </h4>
                  <div className="rounded-lg border border-gray-200 bg-white p-5 text-sm leading-8 whitespace-pre-wrap text-gray-800">
                    {selectedItem.content}
                  </div>
                </div>

                {selectedItem.subContent && (
                  <div>
                    <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                      <LayoutTemplate className="text-primary h-4 w-4" /> Sub Content
                    </h4>
                    <div className="rounded-lg border border-gray-200 bg-gray-50/50 p-5 font-mono text-sm leading-7 whitespace-pre-wrap text-gray-600">
                      {selectedItem.subContent}
                    </div>
                  </div>
                )}

                {selectedItem.context && (
                  <div>
                    <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                      <Layers className="text-primary h-4 w-4" /> Context (검색 벡터 원문)
                    </h4>
                    <div className="rounded-lg border border-gray-200 bg-gray-50/50 p-5 text-xs leading-6 whitespace-pre-wrap text-gray-500">
                      {selectedItem.context}
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end gap-2 border-t border-gray-100 bg-gray-50 px-6 py-4">
              <button
                onClick={() => setIsModalOpen(false)}
                className="rounded-lg border border-gray-300 bg-white px-5 py-2 text-sm font-bold text-gray-700 shadow-sm hover:bg-gray-50"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 3: 기존 keyword/vector 페이지 삭제**

```bash
rm "frontend/app/(main)/search/keyword/page.tsx"
rm "frontend/app/(main)/search/vector/page.tsx"
```

키워드, 벡터 서브 디렉토리도 비었으면 삭제:

```bash
rmdir "frontend/app/(main)/search/keyword" 2>/dev/null || true
rmdir "frontend/app/(main)/search/vector" 2>/dev/null || true
```

- [ ] **Step 4: 커밋**

```bash
git add frontend/app/\(main\)/search/page.tsx \
        frontend/public/const/menu.ts
git rm "frontend/app/(main)/search/keyword/page.tsx" \
       "frontend/app/(main)/search/vector/page.tsx"
git commit -m "feat: 검색 페이지 통합 (keyword/vector → search/page.tsx)"
```

---

## 셀프 리뷰

**1. 스펙 커버리지:**
- [x] GET /collection → Task 1
- [x] GET /search/category → Task 2
- [x] api/search.ts → Task 3
- [x] search/page.tsx 통합 → Task 4
- [x] keyword/vector 페이지 삭제 → Task 4 Step 3
- [x] menu.ts path 수정 → Task 4 Step 1
- [x] 라디오 3개 (KEYWORD / VECTOR / HYBRID) → Task 4 Step 2
- [x] 리랭킹 체크박스 → Task 4 Step 2 (UI만, API 호출은 TODO)
- [x] 컬렉션 드롭다운 → Task 4 Step 2
- [x] 카테고리 다중 선택, 전체 선택 기본값 → Task 4 Step 2
- [x] aliases 전체 선택 시 빈 배열 → Task 4 Step 2 handleSearch

**2. Placeholder 없음:** 리랭킹 API 호출 미구현은 스펙에서 명시적으로 "추후 구현"으로 합의된 사항이며 console.log와 TODO 주석으로 명확히 표시.

**3. 타입 일관성:**
- `SearchResultItem.chunkId: number` → `key={item.chunkId}` 일치
- `SearchRequestBody.aliases: string[]` → `aliases: selectedCodes.length === categories.length ? [] : selectedCodes` 일치
- `getCollectionsApi` 반환 `SearchCollection[]` → `collectionId`, `collectionName` 사용처 일치
- `getSearchCategoriesApi` 반환 `SearchCategory[]` → `code`, `name` 사용처 일치
