import { http } from '@/utils/http'

export function fetchAgents(params) {
  return http.get('/api/agents', { params })
}

export function createAgent(payload) {
  return http.post('/api/agents', payload)
}

export function updateAgent(id, payload) {
  return http.put(`/api/agents/${id}`, payload)
}

export function publishAgent(id) {
  return http.post(`/api/agents/${id}/publish`)
}

export function testAgent(id, payload) {
  return http.post(`/api/agents/${id}/test`, payload)
}

