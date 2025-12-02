<template>
  <div class="console-layout">
    <van-nav-bar
      title="智能体创作平台"
      left-text="返回登录"
      left-arrow
      @click-left="backToLogin"
      right-text="退出登录"
      @click-right="handleLogout"
    />

    <div class="console-body">
      <aside class="console-menu">
        <div
          v-for="item in menuItems"
          :key="item.name"
          :class="['menu-item', activeRoute === item.name ? 'active' : '']"
          @click="go(item)"
        >
          <div class="menu-icon">{{ item.icon }}</div>
          <div class="menu-text">
            <p class="menu-title">{{ item.label }}</p>
            <p class="menu-desc">{{ item.desc }}</p>
          </div>
        </div>
      </aside>

      <section class="console-content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menuItems = [
  {
    name: 'Agents',
    path: '/console/agents',
    icon: '📝',
    label: '智能体编辑器',
    desc: '创建 / 编辑 / 测试智能体'
  },
  {
    name: 'Workflows',
    path: '/console/workflows',
    icon: '🔄',
    label: '工作流设计器',
    desc: '可视化编排与模拟'
  },
  {
    name: 'Knowledge',
    path: '/console/knowledge',
    icon: '📚',
    label: '知识库管理',
    desc: '文档上传与向量化'
  },
  {
    name: 'Plugins',
    path: '/console/plugins',
    icon: '🔌',
    label: '插件配置',
    desc: '插件注册与状态'
  }
]

const activeRoute = computed(() => route.name)

function go(item) {
  if (route.path !== item.path) {
    router.push(item.path)
  }
}

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}

function backToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.console-layout {
  min-height: 100vh;
  background-color: #f5f7fb;
  display: flex;
  flex-direction: column;
}

.console-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
}

.console-menu {
  width: 280px;
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 10px 30px rgba(15, 27, 57, 0.08);
}

.menu-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.menu-item + .menu-item {
  margin-top: 8px;
}

.menu-item:hover {
  background: #f5f7ff;
}

.menu-item.active {
  background: linear-gradient(135deg, #6a93ff, #7d76ff);
  color: #fff;
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.35);
}

.menu-item.active .menu-desc {
  color: rgba(255, 255, 255, 0.7);
}

.menu-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(106, 147, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.menu-item.active .menu-icon {
  background: rgba(255, 255, 255, 0.2);
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
}

.menu-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #7c7c8c;
}

.console-content {
  flex: 1;
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(15, 27, 57, 0.08);
  overflow-y: auto;
}

@media (max-width: 1024px) {
  .console-body {
    flex-direction: column;
  }
  .console-menu {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 8px;
  }
  .menu-item {
    margin-top: 0;
  }
}
</style>

