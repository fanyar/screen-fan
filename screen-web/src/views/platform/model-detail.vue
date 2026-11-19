<template>
  <div v-loading="loading" class="platform-page platform-page--detail model-detail">
    <header class="platform-page-header">
      <div>
        <button class="back-button" type="button" @click="returnToList('/platform/models')">
          <el-icon><ArrowLeft/></el-icon>
          <span>返回列表</span>
        </button>
        <h1 class="platform-page-title">{{ model.title || '模型详情' }}</h1>
        <p class="platform-page-desc">{{ model.category }} · 更新时间 {{ formatTime(model.updatedAt) }}</p>
      </div>
    </header>

    <div class="model-layout">
      <ModelViewer
        :src="model.fileUrl || ''"
        :progress-nodes="viewerNodes"
        :selected-node-id="selectedNodeId"
        :status-filter="statusFilter"
        :focus-node-id="focusNodeId"
        :isolate-selected="isolateSelected"
        :clip-enabled="clipEnabled"
        :clip-position="clipPosition"
        :explode="explode"
        @loaded="viewerReady = true"
        @select="selectNode"
        @binding-report="handleBindingReport"
      />

      <aside class="model-panel">
        <h2>模型信息</h2>
        <dl>
          <div>
            <dt>分类</dt>
            <dd>{{ model.category }}</dd>
          </div>
          <div>
            <dt>格式</dt>
            <dd>GLB</dd>
          </div>
          <div>
            <dt>状态</dt>
            <dd>{{ MODEL_STATUS_LABELS[model.status] || '-' }}</dd>
          </div>
        </dl>

        <div class="progress-summary">
          <div class="summary-stage">
            <span>当前阶段</span>
            <strong>{{ currentStage }}</strong>
          </div>
          <el-progress
            :percentage="summary.progressPercent"
            :stroke-width="10"
            :color="summary.progressPercent < 100 ? '#f97316' : '#22c55e'"
          />
          <div class="summary-counts">
            <span v-for="(label, status) in PROGRESS_STATUS_LABELS" :key="status">
              <i :style="{background: PROGRESS_STATUS_COLORS[status]}"></i>
              {{ label }} {{ summary[summaryField(status)] }}
            </span>
          </div>
        </div>

        <div v-if="selectedNode" class="selected-node">
          <div class="selected-title">
            <i :style="{background: PROGRESS_STATUS_COLORS[selectedNode.displayStatus]}"></i>
            <strong>{{ selectedNode.path }}</strong>
          </div>
          <dl>
            <div>
              <dt>类型</dt>
              <dd>{{ PROGRESS_NODE_TYPE_LABELS[selectedNode.nodeType] }}</dd>
            </div>
            <div>
              <dt>完成率</dt>
              <dd>{{ selectedNode.progressPercent }}%</dd>
            </div>
            <div>
              <dt>计划</dt>
              <dd>{{ selectedNode.plannedStartDate || '-' }} 至 {{ selectedNode.plannedEndDate || '-' }}</dd>
            </div>
            <div>
              <dt>实际</dt>
              <dd>{{ selectedNode.actualStartDate || '-' }} 至 {{ selectedNode.actualEndDate || '-' }}</dd>
            </div>
            <div>
              <dt>负责人</dt>
              <dd>{{ selectedNode.owner || '-' }}</dd>
            </div>
            <div>
              <dt>绑定</dt>
              <dd class="mesh-key">{{ selectedNode.meshKey || '-' }}</dd>
            </div>
          </dl>
          <p v-if="selectedNode.delayReason" class="delay-reason">{{ selectedNode.delayReason }}</p>
          <p v-if="selectedNode.remark" class="node-remark">{{ selectedNode.remark }}</p>
        </div>

        <p v-if="bindingReport.unboundMembers.length" class="binding-warning">
          {{ bindingReport.unboundMembers.length }} 个构件未匹配模型
        </p>
        <p v-if="viewerReady" class="viewer-tip">可拖拽旋转，滚轮缩放，右键平移。</p>
      </aside>
    </div>

    <section class="progress-board">
      <header class="board-header">
        <h2>进度结构</h2>
        <div class="board-tools">
          <el-select v-model="unitFilter" clearable placeholder="全部联" size="small">
            <el-option
              v-for="unit in unitNodes"
              :key="unit.id"
              :label="unit.name"
              :value="unit.id"
            />
          </el-select>
          <el-input
            v-model.trim="keyword"
            clearable
            placeholder="搜索构件"
            size="small"
            prefix-icon="Search"
          />
          <el-button
            size="small"
            :type="isolateSelected ? 'primary' : 'default'"
            :disabled="!selectedNodeId"
            @click="isolateSelected = !isolateSelected"
          >
            <el-icon><Aim/></el-icon>
            <span>隔离</span>
          </el-button>
        </div>
      </header>

      <div class="status-filter">
        <button
          class="status-filter-button"
          :class="{active: !statusFilter}"
          type="button"
          @click="statusFilter = ''"
        >全部</button>
        <button
          v-for="(label, status) in PROGRESS_STATUS_LABELS"
          :key="status"
          class="status-filter-button"
          :class="{active: statusFilter === status}"
          type="button"
          @click="statusFilter = status"
        >
          <i :style="{background: PROGRESS_STATUS_COLORS[status]}"></i>
          {{ label }}
        </button>
      </div>

      <div class="advanced-tools">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="actual">实际</el-radio-button>
          <el-radio-button label="planned">计划</el-radio-button>
          <el-radio-button label="deviation">偏差</el-radio-button>
        </el-radio-group>
        <label class="range-control">
          <el-checkbox v-model="clipEnabled" size="small">剖切</el-checkbox>
          <input v-model.number="clipPosition" :disabled="!clipEnabled" max="1" min="0" step="0.01" type="range">
        </label>
        <el-checkbox v-model="explode" size="small">爆炸</el-checkbox>
      </div>

      <el-alert
          v-if="summary.delayed"
          class="delay-alert"
          :title="`${summary.delayed} 个构件延期`"
          :description="delayedNodeText"
          type="error"
          show-icon
          :closable="false"
      />

      <div v-if="timelineDates.length" class="timeline">
        <div class="timeline-controls">
          <el-button size="small" @click="stepTimeline(-1)">
            <el-icon><ArrowLeft/></el-icon>
          </el-button>
          <el-button size="small" :type="playing ? 'warning' : 'primary'" @click="togglePlay">
            <el-icon><component :is="playing ? 'VideoPause' : 'VideoPlay'"/></el-icon>
            <span>{{ playing ? '暂停' : '回放' }}</span>
          </el-button>
          <el-button size="small" @click="stepTimeline(1)">
            <el-icon><ArrowRight/></el-icon>
          </el-button>
          <el-button size="small" @click="resetTimeline">
            <el-icon><RefreshLeft/></el-icon>
            <span>当前</span>
          </el-button>
          <output>{{ playbackDate || '当前数据' }}</output>
        </div>
        <input
          v-model.number="timelineIndex"
          class="timeline-range"
          :max="timelineDates.length - 1"
          min="-1"
          step="1"
          type="range"
        >
      </div>

      <div class="progress-tree">
        <button
          v-for="node in visibleNodes"
          :key="node.id"
          class="progress-node"
          :class="{selected: node.id === selectedNodeId}"
          :style="{paddingLeft: `${14 + node.depth * 20}px`}"
          type="button"
          @click="selectNode(node.id, true)"
        >
          <i :style="{background: PROGRESS_STATUS_COLORS[node.displayStatus]}"></i>
          <span class="node-name">{{ node.name }}</span>
          <span class="node-type">{{ PROGRESS_NODE_TYPE_LABELS[node.nodeType] }}</span>
          <span class="node-percent">{{ node.progressPercent }}%</span>
          <span class="node-date">{{ node.plannedEndDate || '-' }}</span>
        </button>
        <div v-if="!visibleNodes.length" class="tree-empty">暂无匹配构件</div>
      </div>
    </section>

    <el-alert
      v-if="loadFailed"
      title="模型详情加载失败"
      :description="errorMessage"
      type="error"
      show-icon
      :closable="false"
    />
    <el-alert
      v-else-if="progressLoadFailed"
      title="模型进度加载失败"
      description="模型仍可预览，进度结构暂不可用。"
      type="warning"
      show-icon
      :closable="false"
    />
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {Aim, ArrowLeft, ArrowRight, RefreshLeft, VideoPause, VideoPlay} from '@element-plus/icons-vue'
import ModelViewer from '@/components/model/ModelViewer.vue'
import {getModel, getModelProgress, getModelProgressSnapshots} from '@/api/platform'
import {MODEL_STATUS_LABELS} from '@/const/model'
import {
  PROGRESS_NODE_TYPE_LABELS,
  PROGRESS_STATUS,
  PROGRESS_STATUS_COLORS,
  PROGRESS_STATUS_LABELS,
  calculateProgressSummary,
  deviationStatus,
  flattenProgressNodes,
  plannedStatusAtDate,
  statusAtDate
} from '@/const/progress'
import {preservedListRouteQuery} from '@/util/list-query'

const route = useRoute()
const router = useRouter()

const returnToList = path => router.push({
  path,
  query: preservedListRouteQuery(route.query)
})
const model = ref({})
const progressNodes = ref([])
const backendSummary = ref({})
const snapshots = ref([])
const loading = ref(false)
const loadFailed = ref(false)
const progressLoadFailed = ref(false)
const errorMessage = ref('')
const viewerReady = ref(false)
const selectedNodeId = ref('')
const focusNodeId = ref('')
const statusFilter = ref('')
const unitFilter = ref('')
const keyword = ref('')
const isolateSelected = ref(false)
const timelineIndex = ref(-1)
const playing = ref(false)
const viewMode = ref('actual')
const clipEnabled = ref(false)
const clipPosition = ref(0.5)
const explode = ref(false)
const bindingReport = ref({boundCount: 0, unboundMembers: [], modelMeshCount: 0})
let playbackTimer = null

const loadModel = async () => {
  loading.value = true
  loadFailed.value = false
  progressLoadFailed.value = false
  try {
    const [modelResult, progressResult, snapshotResult] = await Promise.allSettled([
      getModel(route.params.id),
      getModelProgress(route.params.id),
      getModelProgressSnapshots(route.params.id)
    ])
    if (modelResult.status === 'rejected') throw modelResult.reason
    model.value = modelResult.value
    if (progressResult.status === 'fulfilled') {
      progressNodes.value = progressResult.value.nodes || []
      backendSummary.value = progressResult.value.summary || {}
    } else {
      progressNodes.value = []
      backendSummary.value = {}
      progressLoadFailed.value = true
    }
    snapshots.value = snapshotResult.status === 'fulfilled' ? snapshotResult.value : []
    const leaves = flattenProgressNodes(progressNodes.value).filter(node => node.nodeType === 'member')
    selectedNodeId.value = leaves.find(node => node.displayStatus === PROGRESS_STATUS.DELAYED)?.id
      || leaves.find(node => node.displayStatus === PROGRESS_STATUS.IN_PROGRESS)?.id
      || leaves[0]?.id
      || ''
  } catch (error) {
    model.value = {}
    loadFailed.value = true
    errorMessage.value = error.response?.data?.message || '请确认模型已发布，或稍后重试。'
  } finally {
    loading.value = false
  }
}

const timelineDates = computed(() => {
  const dates = new Set()
  flattenProgressNodes(progressNodes.value).forEach(node => {
    ;['plannedStartDate', 'plannedEndDate', 'actualStartDate', 'actualEndDate'].forEach(field => {
      if (node[field]) dates.add(node[field])
    })
  })
  return Array.from(dates).filter(date => date <= new Date().toISOString().slice(0, 10)).sort()
})

const playbackDate = computed(() => (
  timelineIndex.value >= 0 && timelineIndex.value < timelineDates.value.length
    ? timelineDates.value[timelineIndex.value]
    : ''
))

const withPlaybackStatus = (nodes, statusMode = 'actual') => nodes.map(node => {
  const children = withPlaybackStatus(node.children || [], statusMode)
  const actualStatus = statusAtDate(node, playbackDate.value)
  const plannedStatus = plannedStatusAtDate(node, playbackDate.value)
  const displayStatus = statusMode === 'planned'
    ? plannedStatus
    : statusMode === 'deviation'
      ? deviationStatus(actualStatus, plannedStatus)
      : actualStatus
  let progressPercent = Number(node.progressPercent || 0)
  if (displayStatus === PROGRESS_STATUS.COMPLETED) progressPercent = 100
  if (displayStatus === PROGRESS_STATUS.NOT_STARTED) progressPercent = 0
  return {...node, children, displayStatus, progressPercent}
})

const aggregateNodes = nodes => nodes.map(node => {
  const children = aggregateNodes(node.children || [])
  if (!children.length) return node
  const statuses = children.map(child => child.displayStatus)
  const displayStatus = statuses.includes(PROGRESS_STATUS.DELAYED)
    ? PROGRESS_STATUS.DELAYED
    : statuses.includes(PROGRESS_STATUS.IN_PROGRESS)
      ? PROGRESS_STATUS.IN_PROGRESS
      : statuses.every(status => status === PROGRESS_STATUS.COMPLETED)
        ? PROGRESS_STATUS.COMPLETED
        : PROGRESS_STATUS.NOT_STARTED
  return {...node, children, displayStatus, progressPercent: calculateProgressSummary(children).progressPercent}
})

const effectiveNodes = computed(() => aggregateNodes(withPlaybackStatus(progressNodes.value, viewMode.value)))
const flattenedNodes = computed(() => flattenProgressNodes(effectiveNodes.value))
const unitNodes = computed(() => flattenedNodes.value.filter(node => node.nodeType === 'unit'))

const filteredTree = computed(() => {
  if (!unitFilter.value) return effectiveNodes.value
  const unit = unitNodes.value.find(node => node.id === unitFilter.value)
  return unit ? [unit] : []
})

const viewerNodes = computed(() => {
  if (!keyword.value) return filteredTree.value
  const text = keyword.value.toLowerCase()
  const filterNodes = nodes => nodes
    .map(node => {
      const children = filterNodes(node.children || [])
      const self = `${node.name} ${node.meshKey || ''}`.toLowerCase().includes(text)
      return self || children.length ? {...node, children} : null
    })
    .filter(Boolean)
  return filterNodes(filteredTree.value)
})

const visibleNodes = computed(() => {
  let nodes = flattenProgressNodes(viewerNodes.value)
  if (statusFilter.value) nodes = nodes.filter(node => node.displayStatus === statusFilter.value)
  return nodes
})

const summary = computed(() => calculateProgressSummary(effectiveNodes.value))
const selectedNode = computed(() => flattenedNodes.value.find(node => node.id === selectedNodeId.value))
const delayedNodeText = computed(() => flattenedNodes.value
  .filter(node => node.nodeType === 'member' && node.displayStatus === PROGRESS_STATUS.DELAYED)
  .map(node => `${node.path}${node.delayReason ? `：${node.delayReason}` : ''}`)
  .slice(0, 6)
  .join('；'))

const currentStage = computed(() => {
  if (!playbackDate.value && viewMode.value === 'actual') {
    return backendSummary.value.currentStage || '暂无进度数据'
  }
  const leaves = flattenedNodes.value.filter(node => node.nodeType === 'member')
  const current = leaves.find(node => node.displayStatus === PROGRESS_STATUS.DELAYED)
    || leaves.find(node => node.displayStatus === PROGRESS_STATUS.IN_PROGRESS)
    || leaves.filter(node => node.displayStatus === PROGRESS_STATUS.COMPLETED).at(-1)
  return current ? `${current.path}（${PROGRESS_STATUS_LABELS[current.displayStatus]}）` : playbackDate.value
})

const selectNode = (nodeId, focus = false) => {
  selectedNodeId.value = nodeId
  if (focus) {
    focusNodeId.value = ''
    requestAnimationFrame(() => {
      focusNodeId.value = nodeId
    })
  }
}

const handleBindingReport = report => {
  bindingReport.value = report
}

const summaryField = status => ({
  [PROGRESS_STATUS.COMPLETED]: 'completed',
  [PROGRESS_STATUS.IN_PROGRESS]: 'inProgress',
  [PROGRESS_STATUS.NOT_STARTED]: 'notStarted',
  [PROGRESS_STATUS.DELAYED]: 'delayed'
})[status]

const stopPlay = () => {
  playing.value = false
  if (playbackTimer) {
    clearInterval(playbackTimer)
    playbackTimer = null
  }
}

const togglePlay = () => {
  if (playing.value) {
    stopPlay()
    return
  }
  if (timelineIndex.value < 0) timelineIndex.value = 0
  playing.value = true
  playbackTimer = setInterval(() => {
    if (timelineIndex.value >= timelineDates.value.length - 1) {
      stopPlay()
      return
    }
    timelineIndex.value += 1
  }, 500)
}

const stepTimeline = direction => {
  stopPlay()
  const next = timelineIndex.value + direction
  timelineIndex.value = Math.min(Math.max(next, -1), timelineDates.value.length - 1)
}

const resetTimeline = () => {
  stopPlay()
  timelineIndex.value = -1
}

const formatTime = value => String(value || '').replace('T', ' ').slice(0, 16)

watch(() => route.params.id, loadModel)
watch(playing, value => {
  if (!value && playbackTimer) {
    clearInterval(playbackTimer)
    playbackTimer = null
  }
})

onMounted(loadModel)
onBeforeUnmount(stopPlay)
</script>

<style scoped lang="scss">
.model-detail {
  height: auto;
  min-height: 100%;
  overflow: visible;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 12px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--sx-primary);
  font-size: 15px;
  cursor: pointer;
}

.model-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
  height: 560px;
}

.model-panel {
  align-self: stretch;
  min-height: 0;
  overflow-y: auto;
  padding: 20px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);

  h2 {
    margin: 0 0 16px;
    color: var(--sx-title);
    font-size: 18px;
  }

  dl {
    display: grid;
    gap: 10px;
    margin: 0 0 16px;
  }

  dl > div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--sx-line);
  }

  dt {
    flex: 0 0 auto;
    color: var(--sx-muted);
  }

  dd {
    min-width: 0;
    margin: 0;
    color: var(--sx-title);
    font-weight: 600;
    text-align: right;
    overflow-wrap: anywhere;
  }
}

.progress-summary {
  display: grid;
  gap: 12px;
  margin-bottom: 18px;
}

.summary-stage {
  display: grid;
  gap: 4px;

  span {
    color: var(--sx-muted);
    font-size: 13px;
  }

  strong {
    color: var(--sx-title);
    font-size: 16px;
    line-height: 1.4;
  }
}

.summary-counts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  color: var(--sx-body);
  font-size: 13px;

  span {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  i {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.selected-node {
  padding-top: 16px;
  border-top: 1px solid var(--sx-line);

  dl {
    gap: 8px;
  }

  dl > div {
    padding-bottom: 8px;
  }
}

.selected-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  i {
    flex: 0 0 auto;
    width: 9px;
    height: 9px;
    border-radius: 50%;
  }

  strong {
    color: var(--sx-title);
    font-size: 15px;
    line-height: 1.4;
  }
}

.delay-reason {
  margin: 10px 0 0;
  padding: 8px 10px;
  border-radius: 5px;
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
  font-size: 13px;
}

.node-remark {
  margin: 8px 0 0;
  color: var(--sx-muted);
  font-size: 13px;
  line-height: 1.6;
}

.binding-warning,
.viewer-tip {
  margin: 14px 0 0;
  color: var(--sx-muted);
  font-size: 13px;
}

.binding-warning {
  color: #d97706;
}

.progress-board {
  margin-top: 20px;
  padding: 18px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
}

.board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 14px;

  h2 {
    margin: 0;
    color: var(--sx-title);
    font-size: 18px;
  }
}

.board-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  .el-select {
    width: 118px;
  }

  .el-input {
    width: 168px;
  }
}

.status-filter {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.status-filter-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 28px;
  padding: 0 10px;
  border: 1px solid var(--sx-line);
  border-radius: 5px;
  background: transparent;
  color: var(--sx-body);
  font-size: 13px;
  cursor: pointer;

  &.active {
    border-color: var(--sx-primary);
    background: rgba(37, 99, 235, 0.1);
    color: var(--sx-primary);
  }

  i {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.advanced-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.range-control {
  display: inline-flex;
  align-items: center;
  gap: 8px;

  input {
    width: 136px;
    accent-color: var(--sx-primary);
  }
}

.delay-alert {
  margin-bottom: 12px;
}

.timeline {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.03);
  margin-bottom: 14px;
}

.timeline-controls {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;

  output {
    margin-left: auto;
    color: var(--sx-title);
    font-size: 13px;
    font-variant-numeric: tabular-nums;
  }
}

.timeline-range {
  width: 100%;
  accent-color: var(--sx-primary);
}

.progress-tree {
  display: grid;
  gap: 6px;
}

.progress-node {
  display: grid;
  grid-template-columns: 10px minmax(110px, 1.3fr) 42px 54px 88px;
  align-items: center;
  gap: 10px;
  min-height: 38px;
  padding: 7px 12px 7px 14px;
  border: 1px solid transparent;
  border-radius: 5px;
  background: rgba(148, 163, 184, 0.08);
  color: var(--sx-body);
  font-size: 13px;
  text-align: left;
  cursor: pointer;

  &:hover {
    border-color: rgba(37, 99, 235, 0.28);
    background: rgba(37, 99, 235, 0.07);
  }

  &.selected {
    border-color: var(--sx-primary);
    background: rgba(37, 99, 235, 0.12);
  }

  i {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.node-name {
  min-width: 0;
  color: var(--sx-title);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-type,
.node-percent,
.node-date {
  color: var(--sx-muted);
  font-variant-numeric: tabular-nums;
}

.tree-empty {
  padding: 28px 0;
  color: var(--sx-muted);
  text-align: center;
}

@media (max-width: 1000px) {
  .model-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .model-layout > :first-child {
    height: 420px;
    min-height: 420px;
  }
}

@media (max-width: 640px) {
  .board-header,
  .board-tools,
  .status-filter,
  .advanced-tools,
  .timeline-controls {
    align-items: stretch;
    flex-direction: column;
  }

  .board-tools .el-select,
  .board-tools .el-input,
  .board-tools .el-button {
    width: 100%;
  }

  .timeline-controls output {
    margin-left: 0;
  }

  .progress-node {
    grid-template-columns: 10px minmax(0, 1fr) 46px;
  }

  .node-type,
  .node-date {
    display: none;
  }
}
</style>
