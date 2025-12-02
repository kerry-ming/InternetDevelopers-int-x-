<template>
  <div class="console-panel">
    <header class="panel-header">
      <div>
        <h2>工作流设计器</h2>
        <p>使用可视化节点描述智能体执行流程，并同步保存到本地</p>
      </div>
      <van-button type="primary" size="small" @click="loadWorkflows">刷新列表</van-button>
    </header>

    <section class="panel-section">
      <h3>节点画布</h3>
      <div class="canvas">
        <div
          v-for="node in designer.nodes"
          :key="node.id"
          class="node"
          :class="node.type"
          @click="selectNode(node)"
        >
          <p class="node-title">{{ node.label }}</p>
          <p class="node-type">{{ node.type }}</p>
        </div>
        <div class="node create-node" @click="addNode">+ 新增节点</div>
      </div>
    </section>

    <section class="panel-section">
      <h3>节点详情</h3>
      <div v-if="!activeNode" class="empty-tip">请选择画布中的节点进行配置</div>
      <div v-else class="node-form">
        <van-field v-model="activeNode.label" label="节点标题" placeholder="请输入节点名称" />
        <van-field v-model="activeNode.config.prompt" type="textarea" rows="3" label="提示词/说明" />
        <van-field v-model="activeNode.config.extra" label="参数" placeholder="可选参数" />
      </div>
    </section>

    <section class="panel-section">
      <h3>保存 / 执行模拟</h3>
      <van-field v-model="designer.name" label="工作流名称" placeholder="请输入名称" />
      <van-field v-model="designer.description" label="描述" placeholder="可选" />

      <div class="form-actions">
        <van-button type="primary" size="small" @click="save">保存工作流</van-button>
        <van-button size="small" @click="simulate">模拟执行</van-button>
      </div>

      <div class="simulation" v-if="simulationLogs.length">
        <p class="label">模拟日志</p>
        <p v-for="(log, index) in simulationLogs" :key="index">{{ log }}</p>
      </div>
    </section>

    <section class="panel-section">
      <h3>历史记录</h3>
      <div v-if="!workflows.length" class="empty-tip">暂无工作流记录</div>
      <van-cell
        v-for="wf in workflows"
        :key="wf.id"
        :title="wf.name"
        :label="wf.description || '暂无描述'"
        @click="loadWorkflow(wf)"
      >
        <template #value>{{ wf.nodes.length }} 个节点</template>
        <template #right-icon>
          <van-button size="mini" type="danger" @click.stop="removeWorkflow(wf.id)">删除</van-button>
        </template>
      </van-cell>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { showToast } from 'vant'
import { listWorkflows, saveWorkflow, deleteWorkflow } from '@/api/workflow'

const workflows = ref([])
const designer = reactive({
  id: null,
  name: '标准问答流程',
  description: '',
  nodes: [
    { id: 'start', type: 'start', label: '开始', config: {} },
    { id: 'llm', type: 'llm', label: 'LLM 处理', config: { prompt: '请回答用户问题' } },
    { id: 'end', type: 'end', label: '结束', config: {} }
  ]
})

const activeNodeId = ref('start')
const activeNode = computed(() => designer.nodes.find((node) => node.id === activeNodeId.value))
const simulationLogs = ref([])

function addNode() {
  const id = `node_${Date.now()}`
  designer.nodes.push({
    id,
    type: 'llm',
    label: 'LLM 节点',
    config: { prompt: '' }
  })
  activeNodeId.value = id
}

function selectNode(node) {
  activeNodeId.value = node.id
}

async function save() {
  if (!designer.name) {
    showToast('请填写工作流名称')
    return
  }
  const payload = {
    id: designer.id,
    name: designer.name,
    description: designer.description,
    nodes: designer.nodes
  }
  const saved = await saveWorkflow(payload)
  designer.id = saved.id
  showToast('保存成功')
  await loadWorkflows()
}

async function loadWorkflows() {
  workflows.value = await listWorkflows()
}

function loadWorkflow(wf) {
  designer.id = wf.id
  designer.name = wf.name
  designer.description = wf.description
  designer.nodes = wf.nodes.map((node) => ({ ...node }))
  activeNodeId.value = designer.nodes[0]?.id
}

async function removeWorkflow(id) {
  await deleteWorkflow(id)
  showToast('已删除')
  if (designer.id === id) {
    designer.id = null
    designer.name = '标准问答流程'
    designer.description = ''
    designer.nodes = [
      { id: 'start', type: 'start', label: '开始', config: {} },
      { id: 'llm', type: 'llm', label: 'LLM 处理', config: { prompt: '请回答用户问题' } },
      { id: 'end', type: 'end', label: '结束', config: {} }
    ]
  }
  await loadWorkflows()
}

function simulate() {
  simulationLogs.value = designer.nodes.map((node, index) => {
    return `${index + 1}. [${node.type}] ${node.label}`
  })
}

onMounted(() => {
  loadWorkflows()
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

.canvas {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.node {
  min-width: 140px;
  min-height: 80px;
  border-radius: 12px;
  background: #fff;
  padding: 12px;
  box-shadow: 0 6px 16px rgba(61, 73, 102, 0.1);
  cursor: pointer;
  border: 2px solid transparent;
}

.node-start {
  border-color: #67c23a;
}

.node.llm,
.node-llm {
  border-color: #409eff;
}

.node.end,
.node-end {
  border-color: #f56c6c;
}

.node-title {
  font-weight: 600;
}

.node-type {
  font-size: 12px;
  color: #909399;
}

.create-node {
  border: 2px dashed #c0c4cc;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
}

.node-form {
  background: #fff;
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(61, 73, 102, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin: 12px 0;
}

.simulation {
  margin-top: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.simulation .label {
  font-weight: 600;
  margin-bottom: 4px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 24px 0;
}
</style>

