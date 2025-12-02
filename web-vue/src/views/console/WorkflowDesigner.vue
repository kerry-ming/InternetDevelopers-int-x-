<template>
  <div class="page-container">
    <el-card class="card-container">
      <template #header>
        <div class="flex-between">
          <span>工作流设计器</span>
          <el-button type="primary" size="small" @click="handleSave">保存工作流</el-button>
        </div>
      </template>

      <div class="workflow-designer">
        <div class="designer-toolbar">
          <el-button size="small" @click="addNode('start')">开始节点</el-button>
          <el-button size="small" @click="addNode('llm')">LLM节点</el-button>
          <el-button size="small" @click="addNode('condition')">条件节点</el-button>
          <el-button size="small" @click="addNode('plugin')">插件节点</el-button>
          <el-button size="small" @click="addNode('end')">结束节点</el-button>
        </div>

        <div class="designer-canvas" ref="canvasRef">
          <div
            v-for="node in nodes"
            :key="node.id"
            :class="['workflow-node', node.type]"
            :style="{ left: node.x + 'px', top: node.y + 'px' }"
            @mousedown="startDrag(node, $event)"
          >
            <div class="node-header">{{ getNodeLabel(node.type) }}</div>
            <div class="node-content">{{ node.label || node.id }}</div>
          </div>
        </div>
      </div>

      <el-dialog v-model="nodeConfigVisible" title="配置节点" width="500px">
        <el-form :model="currentNode" label-width="100px">
          <el-form-item label="节点标签">
            <el-input v-model="currentNode.label" />
          </el-form-item>
          <el-form-item label="节点配置" v-if="currentNode.type === 'llm'">
            <el-input v-model="currentNode.config.prompt" type="textarea" :rows="3" placeholder="提示词" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="nodeConfigVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNodeConfig">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

interface WorkflowNode {
  id: string
  type: 'start' | 'llm' | 'condition' | 'plugin' | 'end'
  label?: string
  x: number
  y: number
  config?: any
}

const canvasRef = ref<HTMLElement>()
const nodes = ref<WorkflowNode[]>([])
const nodeConfigVisible = ref(false)
const currentNode = reactive<WorkflowNode>({
  id: '',
  type: 'start',
  x: 0,
  y: 0
})

let dragNode: WorkflowNode | null = null
let dragOffset = { x: 0, y: 0 }

function addNode(type: WorkflowNode['type']) {
  const node: WorkflowNode = {
    id: `node_${Date.now()}`,
    type,
    x: 100 + nodes.value.length * 150,
    y: 100,
    config: {}
  }
  nodes.value.push(node)
}

function getNodeLabel(type: string) {
  const labels: Record<string, string> = {
    start: '开始',
    llm: 'LLM',
    condition: '条件',
    plugin: '插件',
    end: '结束'
  }
  return labels[type] || type
}

function startDrag(node: WorkflowNode, event: MouseEvent) {
  dragNode = node
  dragOffset.x = event.clientX - node.x
  dragOffset.y = event.clientY - node.y

  const onMouseMove = (e: MouseEvent) => {
    if (dragNode) {
      dragNode.x = e.clientX - dragOffset.x
      dragNode.y = e.clientY - dragOffset.y
    }
  }

  const onMouseUp = () => {
    dragNode = null
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function saveNodeConfig() {
  nodeConfigVisible.value = false
  ElMessage.success('节点配置已保存')
}

function handleSave() {
  const workflow = {
    nodes: nodes.value,
    edges: [] // TODO: 实现连线逻辑
  }
  console.log('保存工作流:', workflow)
  ElMessage.success('工作流已保存（当前为演示版本）')
}
</script>

<style scoped>
.workflow-designer {
  position: relative;
  height: 600px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}

.designer-toolbar {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.designer-canvas {
  position: relative;
  width: 100%;
  height: calc(100% - 50px);
  overflow: auto;
}

.workflow-node {
  position: absolute;
  min-width: 120px;
  background: #fff;
  border: 2px solid #409eff;
  border-radius: 8px;
  cursor: move;
  user-select: none;
}

.workflow-node.start {
  border-color: #67c23a;
}

.workflow-node.end {
  border-color: #f56c6c;
}

.node-header {
  padding: 8px;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  border-radius: 6px 6px 0 0;
}

.workflow-node.start .node-header {
  background: #67c23a;
}

.workflow-node.end .node-header {
  background: #f56c6c;
}

.node-content {
  padding: 12px;
  font-size: 12px;
  text-align: center;
}
</style>



