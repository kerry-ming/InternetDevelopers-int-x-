<template>
  <div class="console-panel">
    <header class="panel-header">
      <div>
        <h2>知识库管理</h2>
        <p>创建知识库、上传文档并追踪向量化状态</p>
      </div>
      <van-button type="primary" size="small" @click="fetchKnowledgeBases" :loading="loading">刷新</van-button>
    </header>

    <section class="panel-section">
      <h3>创建知识库</h3>
      <van-form @submit="handleCreate">
        <van-cell-group inset>
          <van-field v-model="form.name" label="名称" placeholder="知识库名称" required />
          <van-field v-model="form.description" label="描述" placeholder="可选描述" />
          <van-field v-model.number="form.chunkSize" label="分块大小" type="number" />
          <van-field v-model.number="form.chunkOverlap" label="分块重叠" type="number" />
        </van-cell-group>
        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="creating">新建知识库</van-button>
        </div>
      </van-form>
    </section>

    <section class="panel-section">
      <h3>知识库列表</h3>
      <div class="knowledge-list">
        <div
          v-for="kb in knowledgeBases"
          :key="kb.id"
          :class="['knowledge-card', kb.id === activeId ? 'active' : '']"
          @click="selectKnowledge(kb)"
        >
          <div class="knowledge-card__header">
            <p class="title">{{ kb.name }}</p>
            <van-tag type="primary">chunk {{ kb.chunkSize }}/{{ kb.chunkOverlap }}</van-tag>
          </div>
          <p class="desc">{{ kb.description || '暂无描述' }}</p>
          <p class="meta">创建于 {{ formatDate(kb.createdAt) }}</p>
        </div>
      </div>
    </section>

    <section class="panel-section" v-if="activeKnowledge">
      <div class="section-header">
        <h3>文档列表 · {{ activeKnowledge.name }}</h3>
        <van-uploader :after-read="afterRead" :disabled="uploading" accept=".txt,.md" max-count="1">
          <van-button size="small" icon="plus" type="primary" :loading="uploading">
            上传文档
          </van-button>
        </van-uploader>
      </div>

      <div v-if="!documents.length" class="empty-tip">当前知识库暂无文档</div>

      <van-list>
        <van-cell v-for="doc in documents" :key="doc.id" :title="doc.filename">
          <template #label>
            <p>状态：{{ doc.status }}</p>
            <p>Chunk 数：{{ doc.chunkCount }}</p>
            <p>上传时间：{{ formatDate(doc.uploadedAt) }}</p>
          </template>
        </van-cell>
      </van-list>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import {
  fetchKnowledgeBases,
  createKnowledgeBase,
  fetchKnowledgeDocuments,
  uploadKnowledgeDocument
} from '@/api/knowledge'

const loading = ref(false)
const creating = ref(false)
const uploading = ref(false)
const knowledgeBases = ref([])
const documents = ref([])
const activeId = ref(null)

const form = reactive({
  name: '',
  description: '',
  chunkSize: 512,
  chunkOverlap: 50
})

const activeKnowledge = computed(() => knowledgeBases.value.find((item) => item.id === activeId.value))

async function fetchKnowledge() {
  loading.value = true
  try {
    const data = await fetchKnowledgeBases()
    knowledgeBases.value = data || []
    if (knowledgeBases.value.length && !activeId.value) {
      selectKnowledge(knowledgeBases.value[0])
    }
  } catch (error) {
    console.error('获取知识库失败', error)
  } finally {
    loading.value = false
  }
}

async function selectKnowledge(kb) {
  activeId.value = kb.id
  await fetchDocuments()
}

async function fetchDocuments() {
  if (!activeId.value) return
  try {
    const data = await fetchKnowledgeDocuments(activeId.value)
    documents.value = data || []
  } catch (error) {
    console.error('获取文档列表失败', error)
  }
}

async function handleCreate() {
  if (!form.name) {
    showToast('请填写知识库名称')
    return
  }
  creating.value = true
  try {
    await createKnowledgeBase({ ...form })
    showToast('创建成功')
    form.name = ''
    form.description = ''
    await fetchKnowledge()
  } catch (error) {
    console.error('创建知识库失败', error)
  } finally {
    creating.value = false
  }
}

async function afterRead(file) {
  if (!activeId.value) {
    showToast('请先选择知识库')
    return
  }
  uploading.value = true
  try {
    await uploadKnowledgeDocument(activeId.value, file.file)
    showToast('上传成功')
    await fetchDocuments()
  } catch (error) {
    console.error('上传失败', error)
  } finally {
    uploading.value = false
  }
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchKnowledge()
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

.knowledge-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.knowledge-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: border 0.2s, transform 0.2s;
  border: 1px solid transparent;
  min-height: 130px;
}

.knowledge-card.active {
  border-color: #6a93ff;
  box-shadow: 0 12px 24px rgba(106, 147, 255, 0.2);
}

.knowledge-card__header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.title {
  font-weight: 600;
}

.desc {
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
}

.meta {
  font-size: 12px;
  color: #909399;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 24px 0;
}
</style>

