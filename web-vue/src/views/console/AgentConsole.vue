<template>
  <div class="page-container">
    <el-card class="card-container mb-16">
      <template #header>
        <div class="flex-between">
          <span>创建智能体</span>
          <el-button type="primary" size="small" @click="fetchList" :loading="loading">刷新列表</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" @submit.prevent="handleCreate">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="form.name" placeholder="请输入智能体名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="form.description" placeholder="一句话介绍智能体" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="系统提示词" required>
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="描述AI的身份与能力"
          />
        </el-form-item>
        <el-form-item label="用户模板">
          <el-input
            v-model="form.userPromptTemplate"
            type="textarea"
            :rows="2"
            placeholder="可选：用户消息模板"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模型服务商" required>
              <el-input v-model="form.model.provider" placeholder="如 deepseek" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型名称" required>
              <el-input v-model="form.model.model" placeholder="如 deepseek-chat" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Temperature">
              <el-input-number v-model="form.model.temperature" :min="0" :max="1" :step="0.1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Max Tokens">
              <el-input-number v-model="form.model.maxTokens" :min="32" :max="4096" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="creating" @click="handleCreate">创建智能体</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="card-container">
      <template #header>
        <span>智能体列表</span>
      </template>

      <el-table :data="agents" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="模型配置" width="200">
          <template #default="{ row }">
            <span>{{ row.modelConfig?.provider }} / {{ row.modelConfig?.model }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'warning'">
              {{ row.status || 'draft' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openTest(row)">调试</el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.status === 'published'"
              @click="publish(row)"
            >
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="testVisible" title="调试智能体" width="600px">
      <div>
        <p><strong>智能体：</strong>{{ testingAgent?.name }}</p>
        <el-form-item label="测试输入">
          <el-input
            v-model="testQuestion"
            type="textarea"
            :rows="4"
            placeholder="请填写想测试的问题"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="testing" @click="runTest">发送请求</el-button>
        </el-form-item>

        <div v-if="testResult" class="test-result">
          <p><strong>AI 回复：</strong></p>
          <p>{{ testResult.reply }}</p>
          <p class="meta">
            耗时：{{ testResult.elapsedMs }} ms · Tokens：{{ testResult.promptTokens }} /
            {{ testResult.completionTokens }}
          </p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAgents, createAgent, publishAgent, testAgent, type AgentVO, type AgentTestResponse } from '@/api/agent'

const agents = ref<AgentVO[]>([])
const loading = ref(false)
const creating = ref(false)
const form = reactive({
  name: '',
  description: '',
  systemPrompt: '',
  userPromptTemplate: '',
  model: {
    provider: 'deepseek',
    model: 'deepseek-chat',
    temperature: 0.7,
    maxTokens: 2048
  }
})

const testVisible = ref(false)
const testingAgent = ref<AgentVO | null>(null)
const testQuestion = ref('')
const testResult = ref<AgentTestResponse | null>(null)
const testing = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const data = await fetchAgents({ pageNo: 1, pageSize: 20 })
    agents.value = data?.records || []
  } catch (error) {
    console.error('获取智能体列表失败', error)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!form.name || !form.systemPrompt) {
    ElMessage.warning('请填写必要字段')
    return
  }
  creating.value = true
  try {
    await createAgent({
      name: form.name,
      description: form.description,
      systemPrompt: form.systemPrompt,
      userPromptTemplate: form.userPromptTemplate,
      modelConfig: { ...form.model }
    })
    ElMessage.success('创建成功')
    Object.assign(form, {
      name: '',
      description: '',
      systemPrompt: '',
      userPromptTemplate: ''
    })
    await fetchList()
  } catch (error) {
    console.error('创建智能体失败', error)
  } finally {
    creating.value = false
  }
}

function openTest(agent: AgentVO) {
  testingAgent.value = agent
  testVisible.value = true
  testQuestion.value = ''
  testResult.value = null
}

async function publish(agent: AgentVO) {
  try {
    await publishAgent(agent.id)
    ElMessage.success('发布成功')
    fetchList()
  } catch (error) {
    console.error('发布失败', error)
  }
}

async function runTest() {
  if (!testingAgent.value || !testQuestion.value) {
    ElMessage.warning('请输入测试问题')
    return
  }
  testing.value = true
  try {
    const result = await testAgent(testingAgent.value.id, { question: testQuestion.value })
    testResult.value = result
  } catch (error) {
    console.error('测试失败', error)
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.test-result {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.test-result .meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>



