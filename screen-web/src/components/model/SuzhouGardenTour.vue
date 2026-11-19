<template>
  <div class="suzhou-garden-tour">
    <canvas ref="canvasRef" class="garden-canvas" aria-label="苏州拙政园中部三维漫游"></canvas>

    <template v-if="showUi">
      <header class="garden-top">
        <button type="button" class="control-button" @click="returnToList">返回</button>
        <div class="title-block">
          <strong>苏州拙政园 · 中部园景</strong>
          <span>{{ moving ? '正在切换漫游路径' : activeView.info }}</span>
        </div>
        <button
          type="button"
          class="control-button"
          :class="{'is-active': activeView.id === 'overview'}"
          @click="focusView('overview')"
        >总览</button>
      </header>

      <aside class="garden-panel">
        <p class="panel-eyebrow">{{ activeView.group }} · {{ activeIndex + 1 }}/{{ tourOrder.length }}</p>
        <h2>{{ activeView.label }}</h2>
        <p>{{ activeView.info }}</p>
        <dl>
          <div v-for="spec in specs" :key="spec.label">
            <dt>{{ spec.label }}</dt>
            <dd>{{ spec.value }}</dd>
          </div>
        </dl>
      </aside>

      <footer class="garden-nav">
        <div class="nav-groups">
          <div v-for="group in viewGroups" :key="group" class="nav-group">
            <span>{{ group }}</span>
            <button
              v-for="view in viewsByGroup(group)"
              :key="view.id"
              type="button"
              class="nav-item"
              :class="{'is-active': view.id === activeView.id}"
              @click="focusView(view.id)"
            >{{ view.label }}</button>
          </div>
        </div>
        <div class="nav-actions">
          <button type="button" class="control-button" @click="stepTour(-1)">
            <el-icon><ArrowLeft/></el-icon>
            <span>上一处</span>
          </button>
          <button type="button" class="control-button primary" @click="toggleTour">
            <el-icon><component :is="autoTour ? 'VideoPause' : 'VideoPlay'"/></el-icon>
            <span>{{ autoTour ? '暂停' : '自动漫游' }}</span>
          </button>
          <button type="button" class="control-button" @click="stepTour(1)">
            <span>下一处</span>
            <el-icon><ArrowRight/></el-icon>
          </button>
        </div>
      </footer>

      <p class="keyboard-tip">鼠标拖拽旋转 · 滚轮缩放 · 右键平移 · 空格漫游 · ← → 切换</p>
    </template>
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ArrowLeft, ArrowRight, VideoPause, VideoPlay} from '@element-plus/icons-vue'
import * as THREE from 'three'
import {createGardenScene} from '@/components/model/suzhou-garden-scene'
import {
  SUZHOU_GARDEN_SPECS,
  SUZHOU_GARDEN_TOUR_ORDER,
  SUZHOU_GARDEN_VIEW_GROUPS,
  SUZHOU_GARDEN_VIEWS
} from '@/const/suzhou-garden'
import {preservedListRouteQuery} from '@/util/list-query'

const props = defineProps({
  initialView: {type: String, default: 'overview'},
  showUi: {type: Boolean, default: true}
})

const canvasRef = ref(null)
const activeViewId = ref(props.initialView)
const autoTour = ref(false)
const moving = ref(false)
const loadFailed = ref(false)
const router = useRouter()
const route = useRoute()
const returnToList = () => router.push({
  path: '/platform/models',
  query: preservedListRouteQuery(route.query)
})

const views = SUZHOU_GARDEN_VIEWS
const tourOrder = SUZHOU_GARDEN_TOUR_ORDER
const viewGroups = SUZHOU_GARDEN_VIEW_GROUPS
const specs = SUZHOU_GARDEN_SPECS
const activeView = computed(() => views.find(view => view.id === activeViewId.value) || views[0])
const activeIndex = computed(() => Math.max(0, tourOrder.indexOf(activeViewId.value)))

let garden
let resizeObserver
let cameraAnimation = 0
let autoTimer = 0

const viewsByGroup = group => views.filter(view => view.group === group)

function stopAutoTour() {
  if (!autoTimer) return
  window.clearTimeout(autoTimer)
  autoTimer = 0
  autoTour.value = false
}

function cancelCameraAnimation() {
  if (cameraAnimation) cancelAnimationFrame(cameraAnimation)
  cameraAnimation = 0
  moving.value = false
}

function scheduleTour() {
  window.clearTimeout(autoTimer)
  autoTimer = window.setTimeout(() => {
    autoTimer = 0
    const nextIndex = (activeIndex.value + 1) % tourOrder.length
    focusView(tourOrder[nextIndex], {auto: true})
  }, 4800)
}

function applyControlLimits(view) {
  garden.controls.minDistance = view.minDistance
  garden.controls.maxDistance = view.maxDistance
  garden.controls.maxPolarAngle = view.maxDistance <= 14 ? Math.PI / 1.84 : Math.PI / 2.06
  garden.controls.minPolarAngle = .05
}

function focusView(viewId, options = {}) {
  const view = views.find(item => item.id === viewId)
  if (!view || !garden) return
  if (!options.auto) stopAutoTour()
  if (activeViewId.value === view.id) {
    applyControlLimits(view)
    if (options.auto) scheduleTour()
    return
  }

  if (cameraAnimation) cancelAnimationFrame(cameraAnimation)
  autoTour.value = Boolean(options.auto)
  moving.value = true
  activeViewId.value = view.id
  garden.controls.enabled = false

  const startPosition = garden.camera.position.clone()
  const startTarget = garden.controls.target.clone()
  const endPosition = new THREE.Vector3(...view.camera)
  const endTarget = new THREE.Vector3(...view.target)
  const distance = Math.max(startPosition.distanceTo(endPosition), startTarget.distanceTo(endTarget) * .5)
  const duration = Math.min(2100, Math.max(780, distance * 22))
  const startTime = performance.now()

  const tick = now => {
    const linear = Math.min(1, (now - startTime) / duration)
    const eased = linear < .5
      ? 4 * linear * linear * linear
      : 1 - Math.pow(-2 * linear + 2, 3) / 2
    garden.camera.position.lerpVectors(startPosition, endPosition, eased)
    garden.controls.target.lerpVectors(startTarget, endTarget, eased)
    garden.camera.lookAt(garden.controls.target)
    if (linear < 1) {
      cameraAnimation = requestAnimationFrame(tick)
      return
    }
    cameraAnimation = 0
    moving.value = false
    garden.controls.target.copy(endTarget)
    applyControlLimits(view)
    garden.controls.enabled = !options.auto
    if (options.auto) scheduleTour()
  }
  cameraAnimation = requestAnimationFrame(tick)
}

function stepTour(step) {
  const nextIndex = (activeIndex.value + step + tourOrder.length) % tourOrder.length
  focusView(tourOrder[nextIndex])
}

function toggleTour() {
  if (autoTour.value) {
    stopAutoTour()
    garden.controls.enabled = true
    return
  }
  autoTour.value = true
  scheduleTour()
}

function handleKeydown(event) {
  if (['INPUT', 'TEXTAREA', 'SELECT'].includes(event.target?.tagName)) return
  if (event.code === 'ArrowRight') {
    event.preventDefault()
    stepTour(1)
  } else if (event.code === 'ArrowLeft') {
    event.preventDefault()
    stepTour(-1)
  } else if (event.code === 'Space') {
    event.preventDefault()
    toggleTour()
  } else if (/^Digit[1-8]$/.test(event.code)) {
    const index = Number(event.code.replace('Digit', '')) - 1
    if (tourOrder[index]) focusView(tourOrder[index])
  }
}

function resize() {
  garden?.resize()
}

watch(() => props.initialView, viewId => {
  focusView(viewId)
})

onMounted(async () => {
  await nextTick()
  try {
    garden = createGardenScene(canvasRef.value)
    const initialView = views.find(view => view.id === activeViewId.value) || views[0]
    applyControlLimits(initialView)
    resizeObserver = new ResizeObserver(resize)
    resizeObserver.observe(canvasRef.value)
    window.addEventListener('keydown', handleKeydown)
  } catch (error) {
    loadFailed.value = true
  }
})

onBeforeUnmount(() => {
  stopAutoTour()
  cancelCameraAnimation()
  resizeObserver?.disconnect()
  window.removeEventListener('keydown', handleKeydown)
  garden?.dispose()
})
</script>

<style scoped lang="scss">
.suzhou-garden-tour {
  position: relative;
  min-height: calc(100vh - 112px);
  overflow: hidden;
  background: #cddbe2;
}

.garden-canvas {
  display: block;
  width: 100%;
  height: calc(100vh - 112px);
}

.garden-top {
  position: absolute;
  top: 18px;
  left: 20px;
  right: 20px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 0 0 auto;
  min-width: 64px;
  height: 38px;
  padding: 0 12px;
  border: 1px solid rgba(21, 34, 40, .18);
  border-radius: 6px;
  background: rgba(250, 250, 246, .94);
  color: #3d4a52;
  font-size: 13px;
  cursor: pointer;
  transition: background-color .2s ease, color .2s ease, transform .2s ease;

  &:hover {
    transform: translateY(-1px);
  }

  &.primary,
  &.is-active {
    border-color: #233d3b;
    background: #233d3b;
    color: #f5efe0;
  }
}

.title-block {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
  padding: 8px 13px;
  border: 1px solid rgba(255, 255, 255, .22);
  border-radius: 6px;
  background: rgba(18, 27, 31, .54);
  backdrop-filter: blur(14px);
  color: #fff;

  strong {
    font-size: 16px;
    line-height: 20px;
  }

  span {
    overflow: hidden;
    font-size: 12px;
    line-height: 17px;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: rgba(255, 255, 255, .76);
  }
}

.garden-panel {
  position: absolute;
  top: 82px;
  right: 20px;
  z-index: 3;
  width: 266px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, .18);
  border-radius: 8px;
  background: rgba(17, 26, 30, .62);
  backdrop-filter: blur(18px);
  color: #fff;

  .panel-eyebrow {
    margin: 0;
    color: #d9c69b;
    font-size: 11px;
    letter-spacing: .08em;
  }

  h2 {
    margin: 7px 0 0;
    color: #f4e9cd;
    font-size: 20px;
    line-height: 26px;
  }

  > p {
    min-height: 60px;
    margin: 10px 0 15px;
    color: rgba(255, 255, 255, .75);
    font-size: 13px;
    line-height: 21px;
  }
}

.garden-panel dl {
  display: grid;
  gap: 9px;
  margin: 0;
}

.garden-panel dl div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.garden-panel dt {
  color: rgba(255, 255, 255, .52);
  font-size: 12px;
}

.garden-panel dd {
  margin: 0;
  color: #fff;
  font-size: 12px;
  text-align: right;
}

.garden-nav {
  position: absolute;
  bottom: 40px;
  left: 20px;
  right: 20px;
  z-index: 3;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, .18);
  border-radius: 8px;
  background: rgba(16, 24, 28, .56);
  backdrop-filter: blur(16px);
}

.nav-groups {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 16px;
}

.nav-group {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;

  span {
    flex: 0 0 32px;
    color: rgba(255, 255, 255, .52);
    font-size: 12px;
  }
}

.nav-item {
  flex: 1 1 auto;
  min-width: 42px;
  height: 32px;
  padding: 0 4px;
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: rgba(255, 255, 255, .78);
  font-size: 13px;
  cursor: pointer;
  transition: background-color .2s ease, color .2s ease;

  &:hover {
    background: rgba(255, 255, 255, .12);
    color: #fff;
  }

  &.is-active {
    background: #d9c5a0;
    color: #22343d;
    font-weight: 600;
  }
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.keyboard-tip {
  position: absolute;
  right: 22px;
  bottom: 15px;
  z-index: 3;
  margin: 0;
  color: rgba(255, 255, 255, .64);
  font-size: 11px;
}
</style>
