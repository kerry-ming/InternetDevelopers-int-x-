import { http } from '@/utils/http'

// 获取插件列表
export function listPlugins(params = {}) {
  return http.get('/api/plugin', { params })
}

// 获取插件详情
export function getPlugin(id) {
  return http.get(`/api/plugin/${id}`)
}

// 创建插件
export function createPlugin(plugin) {
  return http.post('/api/plugin', plugin)
}

// 更新插件
export function updatePlugin(id, plugin) {
  return http.put(`/api/plugin/${id}`, plugin)
}

// 删除插件
export function deletePlugin(id) {
  return http.delete(`/api/plugin/${id}`)
}

// 启用插件
export function enablePlugin(id) {
  return http.post(`/api/plugin/${id}/enable`)
}

// 禁用插件
export function disablePlugin(id) {
  return http.post(`/api/plugin/${id}/disable`)
}

// 执行插件函数
export function executePlugin(id, data) {
  return http.post(`/api/plugin/${id}/execute`, data)
}

// 保存插件（兼容旧接口）
export function savePlugin(plugin) {
  if (plugin.id) {
    return updatePlugin(plugin.id, plugin)
  } else {
    return createPlugin(plugin)
  }
}

// 切换插件状态（兼容旧接口）
export function togglePlugin(id) {
  return getPlugin(id).then(plugin => {
    if (plugin.status === 'enabled') {
      return disablePlugin(id)
    } else {
      return enablePlugin(id)
    }
  })
}

