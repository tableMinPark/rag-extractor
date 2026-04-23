import {
  BookOpen,
  FileSearch,
  FileText,
  FlaskConical,
  FolderSearch,
  History,
  Settings,
  WandSparkles,
} from 'lucide-react'

type MenuInfo = {
  name: string
  description: string
  path: string
  icon: typeof BookOpen
  activePaths?: string[]
}

export const menuInfos: Record<string, MenuInfo> = {
  document: {
    name: '문서 조회',
    description: '참고 문서 조회',
    path: '/document',
    icon: BookOpen,
    activePaths: ['/document'],
  },
  extract: {
    name: '문서 추출',
    description: '텍스트, 파일, 원격 문서 추출',
    path: '/extract/text',
    icon: WandSparkles,
    activePaths: ['/extract'],
  },
  source: {
    name: '문서 관리',
    description: '패시지 & 청크 관리',
    path: '/source',
    icon: FileText,
    activePaths: ['/source', '/passage', '/chunk'],
  },
  search: {
    name: 'RAG 검색',
    description: '청크 키워드 & 벡터 검색',
    path: '/search/keyword',
    icon: FileSearch,
    activePaths: ['/search'],
  },
  simulation: {
    name: '시뮬레이션',
    description: '질문 & 답변 시뮬레이션',
    path: '/simulation',
    icon: FlaskConical,
    activePaths: ['/simulation'],
  },
  chatHistory: {
    name: '대화 이력',
    description: '대화 이력',
    path: '/chatHistory',
    icon: History,
    activePaths: ['/chatHistory'],
  },
  prompt: {
    name: '프롬프트 관리',
    description: '프롬프트 관리',
    path: '/prompt',
    icon: FolderSearch,
    activePaths: ['/prompt'],
  },
  setting: {
    name: '설정',
    description: '관리 시스템 설정',
    path: '/setting',
    icon: Settings,
    activePaths: ['/setting'],
  },
}

export const sidebarMenuKeys = [
  'document',
  'extract',
  'source',
  'search',
  'simulation',
  'chatHistory',
  'prompt',
  'setting',
] as const
