import { http } from '@/utils/http'

// 插件类型定义
export interface Plugin {
  id: number | string
  name: string
  type: 'builtin' | 'custom'
  status: 'enabled' | 'disabled'
  description?: string
  openapiSpec: string
  config: string
  createdAt?: string
  updatedAt?: string
}

// 插件执行参数类型
export interface PluginExecuteParams {
  functionName: string
  arguments: Record<string, any>
}

// 插件执行结果类型
export interface PluginExecuteResult {
  pluginId: number | string
  functionName: string
  result: string
}

// 获取插件列表
export function listPlugins(params: Record<string, any> = {}) {
  return http.get<Plugin[]>('/api/plugin', { params })
}

// 获取插件详情
export function getPlugin(id: number | string) {
  return http.get<Plugin>(`/api/plugin/${id}`)
}

// 创建插件
export function createPlugin(plugin: Omit<Plugin, 'id' | 'type' | 'status' | 'createdAt' | 'updatedAt'>) {
  return http.post<Plugin>('/api/plugin', plugin)
}

// 更新插件
export function updatePlugin(id: number | string, plugin: Omit<Plugin, 'id' | 'type' | 'createdAt' | 'updatedAt'>) {
  return http.put<Plugin>(`/api/plugin/${id}`, plugin)
}

// 删除插件
export function deletePlugin(id: number | string) {
  return http.delete(`/api/plugin/${id}`)
}

// 启用插件
export function enablePlugin(id: number | string) {
  return http.post(`/api/plugin/${id}/enable`)
}

// 禁用插件
export function disablePlugin(id: number | string) {
  return http.post(`/api/plugin/${id}/disable`)
}

// 执行插件函数
export function executePlugin(id: number | string, data: PluginExecuteParams) {
  return http.post<PluginExecuteResult>(`/api/plugin/${id}/execute`, data)
}

// 保存插件（兼容旧接口）
export function savePlugin(plugin: Plugin) {
  if (plugin.id) {
    return updatePlugin(plugin.id, plugin)
  } else {
    return createPlugin(plugin)
  }
}

// 切换插件状态（兼容旧接口）
export function togglePlugin(id: number | string) {
  return getPlugin(id).then(plugin => {
    if (plugin.status === 'enabled') {
      return disablePlugin(id)
    } else {
      return enablePlugin(id)
    }
  })
}
