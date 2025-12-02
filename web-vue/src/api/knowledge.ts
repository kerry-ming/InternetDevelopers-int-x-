import { http } from '@/utils/http'

export interface KnowledgeBaseCreateRequest {
  name: string
  description?: string
  chunkSize?: number
  chunkOverlap?: number
}

export interface KnowledgeBaseVO {
  id: number
  name: string
  description?: string
  vectorDbType?: string
  chunkSize?: number
  chunkOverlap?: number
  createdAt: string
  updatedAt: string
}

export interface DocumentVO {
  id: number
  filename: string
  status: string
  chunkCount?: number
  uploadedAt: string
}

export const createKnowledgeBase = (data: KnowledgeBaseCreateRequest): Promise<number> => {
  return http.post('/api/knowledge-bases', data)
}

export const fetchKnowledgeBases = (): Promise<KnowledgeBaseVO[]> => {
  return http.get('/api/knowledge-bases')
}

export const getKnowledgeBase = (id: number): Promise<KnowledgeBaseVO> => {
  return http.get(`/api/knowledge-bases/${id}`)
}

export const uploadKnowledgeDocument = (id: number, file: File): Promise<DocumentVO> => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post(`/api/knowledge-bases/${id}/documents`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const fetchKnowledgeDocuments = (id: number, status?: string): Promise<DocumentVO[]> => {
  return http.get(`/api/knowledge-bases/${id}/documents`, { params: { status } })
}



