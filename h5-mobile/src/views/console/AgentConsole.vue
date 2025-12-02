<template>
  <div class="console-panel">
    <header class="panel-header">
      <div>
        <h2>智能体编辑器</h2>
        <p>创建 / 编辑 / 测试你的专属智能体</p>
      </div>
      <van-button type="primary" size="small" :loading="loading" @click="fetchList">
        刷新列表
      </van-button>
    </header>

    <section class="panel-section">
      <h3>创建智能体</h3>
      <van-form @submit="handleCreate">
        <van-cell-group inset>
          <van-field v-model="form.name" label="名称" placeholder="请输入智能体名称" required />
          <van-field v-model="form.description" label="描述" placeholder="一句话介绍智能体" />
          <van-field
            v-model="form.systemPrompt"
            type="textarea"
            rows="3"
            label="系统提示词"
            placeholder="描述AI的身份与能力"
            required
          />
          <van-field
            v-model="form.userPromptTemplate"
            type="textarea"
            rows="2"
            label="用户模板"
            placeholder="可选：用户消息模板"
          />
          <van-field v-model="form.model.provider" label="模型服务商" placeholder="如 deepseek" required />
          <van-field v-model="form.model.model" label="模型名称" placeholder="如 deepseek-chat" required />
          <van-field
            v-model.number="form.model.temperature"
            label="Temperature"
            type="number"
            placeholder="0 - 1"
          />
          <van-field
            v-model.number="form.model.maxTokens"
            label="Max Tokens"
            type="number"
            placeholder="32 - 4096"
          />
        </van-cell-group>
        <div class="form-actions">
          <van-button type="primary" round block native-type="submit" :loading="creating">创建智能体</van-button>
        </div>
      </van-form>
    </section>

    <section class="panel-section">
      <h3>智能体列表</h3>
      <div v-if="!agents.length" class="empty-tip">暂无智能体，快去创建吧</div>
      <div class="grid">
        <div v-for="agent in agents" :key="agent.id" class="agent-card">
          <div class="agent-card__header">
            <div>
              <p class="agent-title">{{ agent.name }}</p>
              <p class="agent-subtitle">{{ agent.description || '暂无描述' }}</p>
            </div>
            <van-tag :type="agent.status === 'published' ? 'success' : 'warning'">
              {{ agent.status || 'draft' }}
            </van-tag>
          </div>

          <div class="agent-meta">
            <p>模型：{{ agent.modelConfig?.provider }} / {{ agent.modelConfig?.model }}</p>
            <p>温度：{{ agent.modelConfig?.temperature ?? '-' }} · Tokens：{{ agent.modelConfig?.maxTokens ?? '-' }}</p>
          </div>

          <div class="agent-actions">
            <van-button size="small" @click="openTest(agent)">调试</van-button>
            <van-button
              size="small"
              type="primary"
              :disabled="agent.status === 'published'"
              @click="publish(agent)"
            >
              发布
            </van-button>
          </div>
        </div>
      </div>
    </section>

    <van-popup v-model:show="testVisible" position="bottom" round :style="{ height: '70%' }">
      <div class="test-panel">
        <h3>调试：{{ testingAgent?.name }}</h3>
        <van-field
          v-model="testQuestion"
          type="textarea"
          rows="4"
          label="测试输入"
          placeholder="请填写想测试的问题"
        />
        <div class="form-actions">
          <van-button block type="primary" :loading="testing" @click="runTest">发送请求</van-button>
        </div>

        <div class="test-result" v-if="testResult">
          <p class="label">AI 回复</p>
          <p>{{ testResult.reply }}</p>
          <p class="meta">耗时：{{ testResult.elapsedMs }} ms · Tokens：{{ testResult.promptTokens }} / {{ testResult.completionTokens }}</p>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { fetchAgents, createAgent, publishAgent, testAgent } from '@/api/agent'

const agents = ref([])
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
const testingAgent = ref(null)
const testQuestion = ref('')
const testResult = ref(null)
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
    showToast('请填写必要字段')
    return
  }
  creating.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description,
      systemPrompt: form.systemPrompt,
      userPromptTemplate: form.userPromptTemplate,
      modelConfig: { ...form.model }
    }
    await createAgent(payload)
    showToast('创建成功')
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

function openTest(agent) {
  testingAgent.value = agent
  testVisible.value = true
  testQuestion.value = ''
  testResult.value = null
}

async function publish(agent) {
  try {
    await publishAgent(agent.id)
    showToast('发布成功')
    fetchList()
  } catch (error) {
    console.error('发布失败', error)
  }
}

async function runTest() {
  if (!testQuestion.value) {
    showToast('请填写测试问题')
    return
  }
  testing.value = true
  try {
    const data = await testAgent(testingAgent.value.id, { question: testQuestion.value })
    testResult.value = data
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

.panel-header h2 {
  font-size: 22px;
  margin-bottom: 4px;
}

.panel-section {
  background: #f7f9ff;
  border-radius: 16px;
  padding: 16px;
}

.panel-section h3 {
  margin-bottom: 12px;
}

.form-actions {
  margin-top: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.agent-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(15, 27, 57, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.agent-card__header {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.agent-title {
  font-size: 16px;
  font-weight: 600;
}

.agent-subtitle {
  font-size: 13px;
  color: #7c7c8c;
}

.agent-meta {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
}

.agent-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-tip {
  color: #909399;
  padding: 16px 0;
  text-align: center;
}

.test-panel {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-panel h3 {
  margin: 0;
}

.test-result {
  background: #f7f8fa;
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.test-result .label {
  font-weight: 600;
  margin-bottom: 4px;
}

.test-result .meta {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>

