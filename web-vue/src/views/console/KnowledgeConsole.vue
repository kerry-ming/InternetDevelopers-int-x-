<template>
  <div class="page-container">
    <el-card class="card-container mb-16">
      <template #header>
        <div class="flex-between">
          <span>创建知识库</span>
          <el-button type="primary" size="small" @click="fetchKnowledge" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" @submit.prevent="handleCreate">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="form.name" placeholder="知识库名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="form.description" placeholder="可选描述" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分块大小">
              <el-input-number v-model="form.chunkSize" :min="100" :max="2000" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分块重叠">
              <el-input-number v-model="form.chunkOverlap" :min="0" :max="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="creating" @click="handleCreate">新建知识库</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="card-container">
          <template #header>
            <span>知识库列表</span>
          </template>
          <el-scrollbar height="500px">
            <div
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :class="['knowledge-item', kb.id === activeId ? 'active' : '']"
              @click="selectKnowledge(kb)"
            >
              <div class="flex-between">
                <div>
                  <p class="kb-title">{{ kb.name }}</p>
                  <p class="kb-desc">{{ kb.description || '暂无描述' }}</p>
                </div>
                <el-tag type="primary">chunk {{ kb.chunkSize }}/{{ kb.chunkOverlap }}</el-tag>
              </div>
              <p class="kb-meta">创建于 {{ formatDate(kb.createdAt) }}</p>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="card-container" v-if="activeKnowledge">
          <template #header>
            <div class="flex-between">
              <span>文档列表 · {{ activeKnowledge.name }}</span>
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :data="{ knowledgeBaseId: activeId }"
                :before-upload="beforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept=".txt,.md"
              >
                <el-button type="primary" size="small" :loading="uploading">上传文档</el-button>
              </el-upload>
            </div>
          </template>

          <el-table :data="documents" v-loading="docLoading" style="width: 100%">
            <el-table-column prop="filename" label="文件名" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="chunkCount" label="Chunk数" width="100" />
            <el-table-column prop="uploadedAt" label="上传时间" width="180" />
          </el-table>
        </el-card>
        <el-empty v-else description="请选择一个知识库" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchKnowledgeBases,
  createKnowledgeBase,
  fetchKnowledgeDocuments,
  type KnowledgeBaseVO,
  type DocumentVO
} from '@/api/knowledge'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const creating = ref(false)
const uploading = ref(false)
const docLoading = ref(false)
const knowledgeBases = ref<KnowledgeBaseVO[]>([])
const documents = ref<DocumentVO[]>([])
const activeId = ref<number | null>(null)

const form = reactive({
  name: '',
  description: '',
  chunkSize: 512,
  chunkOverlap: 50
})

const activeKnowledge = computed(() => knowledgeBases.value.find((item) => item.id === activeId.value))

const uploadUrl = computed(() => {
  if (!activeId.value) return ''
  return `/api/knowledge-bases/${activeId.value}/documents`
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

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

async function selectKnowledge(kb: KnowledgeBaseVO) {
  activeId.value = kb.id
  await fetchDocuments()
}

async function fetchDocuments() {
  if (!activeId.value) return
  docLoading.value = true
  try {
    const data = await fetchKnowledgeDocuments(activeId.value)
    documents.value = data || []
  } catch (error) {
    console.error('获取文档列表失败', error)
  } finally {
    docLoading.value = false
  }
}

async function handleCreate() {
  if (!form.name) {
    ElMessage.warning('请填写知识库名称')
    return
  }
  creating.value = true
  try {
    await createKnowledgeBase({ ...form })
    ElMessage.success('创建成功')
    form.name = ''
    form.description = ''
    await fetchKnowledge()
  } catch (error) {
    console.error('创建知识库失败', error)
  } finally {
    creating.value = false
  }
}

function beforeUpload(file: File) {
  const isTxtOrMd = file.type === 'text/plain' || file.name.endsWith('.md') || file.name.endsWith('.txt')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isTxtOrMd) {
    ElMessage.error('仅支持TXT和Markdown格式')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  uploading.value = true
  return true
}

async function handleUploadSuccess(_response: any) {
  uploading.value = false
  ElMessage.success('上传成功')
  await fetchDocuments()
}

function handleUploadError() {
  uploading.value = false
  ElMessage.error('上传失败')
}

function getStatusType(status: string) {
  const statusMap: Record<string, string> = {
    success: 'success',
    processing: 'warning',
    failed: 'danger',
    pending: 'info'
  }
  return statusMap[status] || 'info'
}

function formatDate(date: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchKnowledge()
})
</script>

<style scoped>
.knowledge-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.knowledge-item:hover {
  background: #f5f7fa;
}

.knowledge-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.kb-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
}

.kb-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.kb-meta {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>

