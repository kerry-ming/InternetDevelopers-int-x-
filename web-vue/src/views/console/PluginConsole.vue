<template>
  <div class="page-container">
    <el-card class="card-container mb-16">
      <template #header>
        <div class="flex-between">
          <span>注册插件</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px" @submit.prevent="handleCreate">
        <el-form-item label="插件名称" required>
          <el-input v-model="form.name" placeholder="请输入插件名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="插件描述" />
        </el-form-item>
        <el-form-item label="插件类型">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option label="内置" value="builtin" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="OpenAPI规范">
          <el-input
            v-model="form.openapiSpec"
            type="textarea"
            :rows="8"
            placeholder="粘贴OpenAPI 3.0规范JSON内容"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="creating" @click="handleCreate">注册插件</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="card-container">
      <template #header>
        <span>插件列表</span>
      </template>

      <el-table :data="plugins" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="插件名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'builtin' ? 'success' : 'info'">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="togglePlugin(row)">{{ row.status === 'enabled' ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="deletePlugin(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Plugin {
  id: string
  name: string
  description?: string
  type: 'builtin' | 'custom'
  status: 'enabled' | 'disabled'
  openapiSpec?: string
  createdAt?: string
}

const plugins = ref<Plugin[]>([])
const loading = ref(false)
const creating = ref(false)
const form = reactive({
  name: '',
  description: '',
  type: 'custom' as 'builtin' | 'custom',
  openapiSpec: ''
})

// 使用localStorage模拟数据存储
const PLUGIN_STORAGE_KEY = 'console_plugins'

function loadPlugins() {
  const raw = localStorage.getItem(PLUGIN_STORAGE_KEY)
  if (raw) {
    try {
      plugins.value = JSON.parse(raw)
    } catch (e) {
      console.error('解析插件数据失败', e)
      plugins.value = []
    }
  } else {
    plugins.value = []
  }
}

function savePlugins() {
  localStorage.setItem(PLUGIN_STORAGE_KEY, JSON.stringify(plugins.value))
}

async function handleCreate() {
  if (!form.name) {
    ElMessage.warning('请填写插件名称')
    return
  }
  creating.value = true
  try {
    const plugin: Plugin = {
      id: `plg_${Date.now()}`,
      name: form.name,
      description: form.description,
      type: form.type,
      status: 'disabled',
      openapiSpec: form.openapiSpec,
      createdAt: new Date().toISOString()
    }
    plugins.value.push(plugin)
    savePlugins()
    ElMessage.success('注册成功')
    Object.assign(form, {
      name: '',
      description: '',
      openapiSpec: ''
    })
  } catch (error) {
    console.error('注册插件失败', error)
  } finally {
    creating.value = false
  }
}

function togglePlugin(plugin: Plugin) {
  plugin.status = plugin.status === 'enabled' ? 'disabled' : 'enabled'
  savePlugins()
  ElMessage.success(`插件已${plugin.status === 'enabled' ? '启用' : '禁用'}`)
}

function deletePlugin(plugin: Plugin) {
  ElMessageBox.confirm('确定要删除该插件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      const index = plugins.value.findIndex((p) => p.id === plugin.id)
      if (index !== -1) {
        plugins.value.splice(index, 1)
        savePlugins()
        ElMessage.success('删除成功')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadPlugins()
})
</script>

<style scoped>
/* 样式已通过全局样式和Element Plus组件提供 */
</style>



