// 테이블 옵션
export interface TableOption {
  page: number
  size: number
}

// 색인 컬렉션
export interface Collection {
  collectionId: string
  collectionName: string
}

// 데이터 카테고리
export interface Category {
  code: string
  name: string
}

// 승인 여부 타입
export type ApproveType =
  | {
      code: 'REQUEST'
      name: '승인요청'
      color: 'blue'
    }
  | {
      code: 'ALLOW'
      name: '승인'
      color: 'green'
    }
  | {
      code: 'DENY'
      name: '반려'
      color: 'red'
    }
  | {
      code: 'WAIT'
      name: '대기'
      color: 'gray'
    }

// 전처리 타입
export type SelectType =
  | {
      code: 'SELECT-TYPE-EMPTY'
      name: '미등록'
      color: 'red'
    }
  | {
      code: 'SELECT-TYPE-TOKEN'
      name: '토큰'
      color: 'green'
    }
  | {
      code: 'SELECT-TYPE-REGEX'
      name: '정규식'
      color: 'blue'
    }
  | {
      code: 'SELECT-TYPE-NONE'
      name: '지정안함'
      color: 'gray'
    }

// 문서
export interface Source {
  sourceId: number
  version: number
  name: string
  collectionId: string
  sourceType: string
  sourceTypeName: string
  categoryCode: string
  categoryName: string
  selectCode: string
  approveCode: string
  isAuto: boolean
  isBatch: boolean
  sysCreateDt: string
  sysModifyDt: string
}

export interface FileResource {
  originFileName: string
  fileSize: number
  ext: string
  url: string
}

export interface RepoResource {
  targetUrl: string
  name: string
}

// 참고 문서
export interface Document {
  id: number
  title: string
  subTitle: string
  thirdTitle: string
  content: string
  subContent: string
  originFileName: string
  url: string
  categoryCode: string
  sourceType: string
  ext: string
}

// 정규식 패턴 타입
export interface PatternType {
  tokenSize: number
  prefixes: PrefixType[]
}

// 정규식 패턴 타입
export interface PrefixType {
  prefix: string
  isTitle: boolean
}

// 패시지
export interface Passage {
  passageId: number
  sourceId: number
  version: number
  title: string
  subTitle: string
  thirdTitle: string
  content: string
  subContent: string
  contentTokenSize: number
  subContentTokenSize: number
  sysCreateDt: string
  sysModifyDt: string
  updateState: string
  sortOrder: number
  parentSortOrder: number
}

// 청크
export interface Chunk {
  chunkId: number
  passageId: number
  version: number
  title: string
  subTitle: string
  thirdTitle: string
  content: string
  compactContent: string
  subContent: string
  contentTokenSize: number
  compactContentTokenSize: number
  subContentTokenSize: number
  sysCreateDt: string
  sysModifyDt: string
}

// 문서 추출 본문
export interface ExtractContent {
  type: string
  content: string
}
