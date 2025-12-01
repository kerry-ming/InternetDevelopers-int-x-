<template>
  <div class="console-panel">
    <header class="panel-header">
      <div>
        <h2>插件配置</h2>
        <p>注册自定义插件，管理启用状态，规范 OpenAPI 配置</p>
      </div>
      <van-button type="primary" size="small" @click="loadPlugins">刷新</van-button>
    </header>

    <section class="panel-section">
      <h3>注册插件</h3>
      <van-form @submit="handleSave">
        <van-cell-group inset>
          <van-field v-model="form.name" label="名称" placeholder="插件名称" required />
          <van-field v-model="form.description" label="描述" placeholder="功能描述" />
          <van-field v-model="form.type" label="类型" placeholder="builtin / custom" />
          <van-field
            v-model="form.openapiSpec"
            type="textarea"
            rows="4"
            label="OpenAPI JSON"
            placeholder='{"openapi":"3.0.0", ...}'
          />
          <van-field
            v-model="form.config"
            type="textarea"
            rows="3"
            label="配置"
            placeholder='{"api_key":"xxx"}'
          />
        </van-cell-group>
        <div class="form-actions">
          <van-button round block type="primary" native-type="submit">保存插件</van-button>
        </div>
      </van-form>
    </section>

    <section class="panel-section">
      <h3>插件列表</h3>
      <div v-if="!plugins.length" class="empty-tip">暂无插件，可先注册一个</div>
      <div class="plugin-list">
        <div v-for="plugin in plugins" :key="plugin.id" class="plugin-card">
          <div class="plugin-header">
            <div>
              <p class="plugin-title">{{ plugin.name }}</p>
              <p class="plugin-desc">{{ plugin.description || '暂无描述' }}</p>
            </div>
            <van-tag :type="plugin.status === 'enabled' ? 'success' : 'warning'">
              {{ plugin.status || 'disabled' }}
            </van-tag>
          </div>

          <p class="meta">类型：{{ plugin.type }}</p>
          <p class="meta">OpenAPI：{{ plugin.openapiSpec?.length ? '已配置' : '未配置' }}</p>

          <div class="plugin-actions">
            <van-button size="small" @click="edit(plugin)">编辑</van-button>
            <van-button size="small" type="primary" @click="toggle(plugin)">
              {{ plugin.status === 'enabled' ? '禁用' : '启用' }}
            </van-button>
            <van-button size="small" type="danger" @click="remove(plugin)">删除</van-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { listPlugins, savePlugin, togglePlugin, deletePlugin } from '@/api/plugin'

const plugins = ref([])
const form = reactive({
  id: null,
  name: '',
  description: '',
  type: 'custom',
  openapiSpec: '',
  config: '',
  status: 'disabled'
})

async function loadPlugins() {
  plugins.value = await listPlugins()
}

function edit(plugin) {
  Object.assign(form, { ...plugin })
}

async function handleSave() {
  if (!form.name) {
    showToast('请填写插件名称')
    return
  }
  const payload = {
    ...form,
    openapiSpec: form.openapiSpec,
    config: form.config
  }
  const saved = await savePlugin(payload)
  showToast('保存成功')
  if (!form.id) {
    form.status = 'disabled'
  }
  form.id = saved.id
  await loadPlugins()
}

async function toggle(plugin) {
  await togglePlugin(plugin.id)
  showToast(plugin.status === 'enabled' ? '已禁用' : '已启用')
  await loadPlugins()
}

async function remove(plugin) {
  await deletePlugin(plugin.id)
  showToast('已删除')
  if (form.id === plugin.id) {
    form.id = null
    form.name = ''
    form.description = ''
    form.openapiSpec = ''
    form.config = ''
  }
  await loadPlugins()
}

onMounted(() => {
  loadPlugins()
})
</script>

<style scoped>
.console-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-section {
  background: #f7f9ff;
  border-radius: 16px;
  padding: 16px;
}

.plugin-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.plugin-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 6px 16px rgba(60, 72, 107, 0.1);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.plugin-header {
  display: flex;
  justify-content: space-between;
}

.plugin-title {
  font-weight: 600;
}

.plugin-desc {
  color: #909399;
  font-size: 13px;
}

.meta {
  font-size: 12px;
  color: #666;
}

.plugin-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 24px 0;
}
</style>

