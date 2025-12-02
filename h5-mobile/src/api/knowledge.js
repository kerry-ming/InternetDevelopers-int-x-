import { http } from '@/utils/http'

export function fetchKnowledgeBases() {
  return http.get('/api/knowledge-bases')
}

export function createKnowledgeBase(payload) {
  return http.post('/api/knowledge-bases', payload)
}

export function fetchKnowledgeBaseDetail(id) {
  return http.get(`/api/knowledge-bases/${id}`)
}

export function fetchKnowledgeDocuments(id, status) {
  return http.get(`/api/knowledge-bases/${id}/documents`, {
    params: { status }
  })
}

export function uploadKnowledgeDocument(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post(`/api/knowledge-bases/${id}/documents`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

