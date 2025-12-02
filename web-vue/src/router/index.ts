import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/console',
    component: () => import('@/views/layout/ConsoleLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/console/agents'
      },
      {
        path: 'agents',
        name: 'Agents',
        component: () => import('@/views/console/AgentConsole.vue'),
        meta: { title: '智能体编辑器', requiresAuth: true }
      },
      {
        path: 'workflows',
        name: 'Workflows',
        component: () => import('@/views/console/WorkflowDesigner.vue'),
        meta: { title: '工作流设计器', requiresAuth: true }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/console/KnowledgeConsole.vue'),
        meta: { title: '知识库管理', requiresAuth: true }
      },
      {
        path: 'plugins',
        name: 'Plugins',
        component: () => import('@/views/console/PluginConsole.vue'),
        meta: { title: '插件配置', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/console/agents'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '智能体创作平台'
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/console/agents')
    return
  }

  next()
})

export default router

