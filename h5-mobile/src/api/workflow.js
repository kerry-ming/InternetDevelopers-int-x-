const WORKFLOW_STORAGE_KEY = 'console_workflows'

function readWorkflows() {
  const raw = localStorage.getItem(WORKFLOW_STORAGE_KEY)
  if (!raw) return []
  try {
    return JSON.parse(raw)
  } catch (e) {
    console.error('解析工作流数据失败', e)
    return []
  }
}

function saveWorkflows(list) {
  localStorage.setItem(WORKFLOW_STORAGE_KEY, JSON.stringify(list))
}

export function listWorkflows() {
  return Promise.resolve(readWorkflows())
}

export function saveWorkflow(workflow) {
  const list = readWorkflows()
  if (workflow.id) {
    const idx = list.findIndex((item) => item.id === workflow.id)
    if (idx !== -1) {
      list[idx] = workflow
    } else {
      list.push(workflow)
    }
  } else {
    workflow.id = `wf_${Date.now()}`
    list.push(workflow)
  }
  saveWorkflows(list)
  return Promise.resolve(workflow)
}

export function deleteWorkflow(id) {
  const list = readWorkflows().filter((item) => item.id !== id)
  saveWorkflows(list)
  return Promise.resolve()
}

