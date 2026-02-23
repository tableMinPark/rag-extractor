import { FileText, FileSearch, FlaskConical, Settings } from 'lucide-react'

export const menuInfos = {
  source: {
    name: '문서 관리',
    description: '패시지 & 청크 관리',
    path: '/source',
    icon: FileText,
  },
  search: {
    name: 'RAG 검색',
    description: '청크 키워드 & 벡터 검색',
    path: '/search',
    icon: FileSearch,
  },
  simulation: {
    name: '시뮬레이션',
    description: '질문 & 답변 시뮬레이션',
    path: '/simulation',
    icon: FlaskConical,
  },
  chatHistory: {
    name: '대화 이력',
    description: '대화 이력',
    path: '/chatHistory',
    icon: FileText,
  },
  prompt: {
    name: '프롬프트 관리',
    description: '프롬프트 관리',
    path: '/prompt',
    icon: FileText,
  },
  setting: {
    name: '설정',
    description: '관리 시스템 설정',
    path: '/setting',
    icon: Settings,
  },
}
