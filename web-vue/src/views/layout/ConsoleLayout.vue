<template>
  <div class="console-layout">
    <el-container>
      <el-header class="console-header">
        <div class="header-left">
          <h2>智能体创作平台</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.nickname || userStore.username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <el-aside width="240px" class="console-sidebar">
          <el-menu
            :default-active="activeRoute"
            router
            class="console-menu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409eff"
          >
            <el-menu-item index="/console/agents">
              <el-icon><Edit /></el-icon>
              <span>智能体编辑器</span>
            </el-menu-item>
            <el-menu-item index="/console/workflows">
              <el-icon><Connection /></el-icon>
              <span>工作流设计器</span>
            </el-menu-item>
            <el-menu-item index="/console/knowledge">
              <el-icon><FolderOpened /></el-icon>
              <span>知识库管理</span>
            </el-menu-item>
            <el-menu-item index="/console/plugins">
              <el-icon><Setting /></el-icon>
              <span>插件配置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="console-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Edit,
  Connection,
  FolderOpened,
  Setting,
  User,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeRoute = computed(() => route.path)

function handleCommand(command: string) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logout()
        router.replace('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.console-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.console-layout :deep(.el-container) {
  height: 100%;
}

.console-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.console-sidebar {
  background: #304156;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.console-menu {
  border-right: none;
  height: 100%;
}

.console-main {
  background: #f5f7fa;
  padding: 20px;
  height: calc(100vh - 64px);
  overflow-y: auto;
}
</style>

