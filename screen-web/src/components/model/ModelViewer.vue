<template>
  <section
    :class="{'model-viewer--expanded': expanded}"
    class="model-viewer"
    aria-label="三维模型进度预览"
  >
    <canvas
      ref="canvasRef"
      class="model-canvas"
      @pointermove="handlePointerMove"
      @pointerleave="hoverNode = null"
      @pointerdown="rememberPointer"
      @click="handleCanvasClick"
    ></canvas>

    <div class="model-legend" aria-label="进度状态图例">
      <span
        v-for="(label, status) in PROGRESS_STATUS_LABELS"
        :key="status"
        class="legend-item"
      >
        <i :style="{background: PROGRESS_STATUS_COLORS[status]}"></i>
        {{ label }}
      </span>
    </div>

    <div class="model-view-controls" aria-label="模型展示区域控制">
      <button
        class="model-view-button"
        type="button"
        :title="expanded ? '还原模型展示区域' : '放大模型展示区域'"
        :aria-label="expanded ? '还原模型展示区域' : '放大模型展示区域'"
        @click="toggleExpanded"
      >
        <el-icon><component :is="expanded ? Fold : Expand"/></el-icon>
      </button>
    </div>

    <div
      v-if="hoverNode"
      class="model-tooltip"
      :style="{left: `${tooltipPosition.x}px`, top: `${tooltipPosition.y}px`}"
    >
      <strong>{{ hoverNode.path || hoverNode.name }}</strong>
      <span>{{ PROGRESS_STATUS_LABELS[hoverNode.displayStatus] || '-' }}</span>
      <span>完成率 {{ hoverNode.progressPercent ?? 0 }}%</span>
    </div>

    <div v-if="loading" class="model-state">模型加载中...</div>
    <el-alert
      v-else-if="errorMessage"
      class="model-state model-state--error"
      title="模型加载失败"
      :description="errorMessage"
      type="error"
      show-icon
      :closable="false"
    />
  </section>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import * as THREE from 'three'
import {Expand, Fold} from '@element-plus/icons-vue'
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js'
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js'
import {DRACOLoader} from 'three/examples/jsm/loaders/DRACOLoader.js'
import {
  PROGRESS_STATUS_COLORS,
  PROGRESS_STATUS_LABELS,
  flattenProgressNodes
} from '@/const/progress'

const props = defineProps({
  src: {type: String, default: ''},
  progressNodes: {type: Array, default: () => []},
  selectedNodeId: {type: String, default: ''},
  statusFilter: {type: String, default: ''},
  focusNodeId: {type: String, default: ''},
  isolateSelected: {type: Boolean, default: false},
  clipEnabled: {type: Boolean, default: false},
  clipPosition: {type: Number, default: 0.5},
  explode: {type: Boolean, default: false}
})

const emit = defineEmits(['loaded', 'error', 'select', 'binding-report'])

const canvasRef = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const hoverNode = ref(null)
const tooltipPosition = reactive({x: 0, y: 0})
const expanded = ref(false)

let renderer = null
let scene = null
let camera = null
let controls = null
let animationId = 0
let resizeObserver = null
let modelGroup = null
let gridHelper = null
let loader = null
let dracoLoader = null
let raycaster = new THREE.Raycaster()
let pointer = new THREE.Vector2()
let pointerDown = reactive({x: 0, y: 0})
const clipPlane = new THREE.Plane(new THREE.Vector3(-1, 0, 0), 0)

const disposeObject = object => {
  object.traverse(item => {
    if (item.geometry) {
      item.geometry.dispose()
    }
    if (item.material) {
      const materials = Array.isArray(item.material) ? item.material : [item.material]
      materials.forEach(material => {
        Object.values(material).forEach(value => {
          if (value?.isTexture) {
            value.dispose()
          }
        })
        material.dispose()
      })
    }
  })
}

const clearModel = () => {
  if (gridHelper) {
    scene.remove(gridHelper)
    gridHelper.geometry.dispose()
    gridHelper.material.dispose()
    gridHelper = null
  }
  if (!modelGroup) return
  scene.remove(modelGroup)
  disposeObject(modelGroup)
  modelGroup = null
  hoverNode.value = null
}

const fitCamera = () => {
  if (!modelGroup) return
  const bounds = new THREE.Box3().setFromObject(modelGroup)
  const size = new THREE.Vector3()
  const center = new THREE.Vector3()
  bounds.getSize(size)
  bounds.getCenter(center)
  const maxDimension = Math.max(size.x, size.y, size.z, 1)
  camera.near = Math.max(maxDimension / 500, 0.01)
  camera.far = maxDimension * 6
  controls.minDistance = Math.max(maxDimension / 40, camera.near * 8)
  controls.maxDistance = maxDimension * 8
  camera.position.copy(center).add(new THREE.Vector3(0.75, 0.5, 1.2).multiplyScalar(maxDimension * 1.3))
  camera.lookAt(center)
  controls.target.copy(center)
  camera.updateProjectionMatrix()
  controls.update()
}

const progressKeyCandidates = object => {
  const keys = []
  let current = object
  while (current) {
    const key = current.userData?.progressKey || current.name
    if (key) {
      keys.push(key)
    }
    current = current.parent
  }
  return keys
}

const progressKeyFor = (object, availableKeys) => (
  progressKeyCandidates(object).find(key => availableKeys.has(key)) || ''
)

const prepareMaterials = mesh => {
  const clone = material => {
    const result = material.clone()
    result.side = THREE.DoubleSide
    result.userData.originalColor = material.color.getHex()
    result.userData.originalEmissive = material.emissive?.getHex() || 0
    result.userData.originalOpacity = material.opacity
    result.userData.originalTransparent = material.transparent
    return result
  }
  mesh.material = Array.isArray(mesh.material)
    ? mesh.material.map(clone)
    : clone(mesh.material)
}

const setMeshStatus = (mesh, status, selected) => {
  const color = new THREE.Color(PROGRESS_STATUS_COLORS[status] || '#94a3b8')
  const materials = Array.isArray(mesh.material) ? mesh.material : [mesh.material]
  materials.forEach(material => {
    material.color.copy(color)
    material.emissive?.copy(color)
    material.emissiveIntensity = selected ? 0.5 : 0.16
    material.transparent = false
    material.opacity = 1
    material.needsUpdate = true
  })
}

const resetMesh = mesh => {
  const materials = Array.isArray(mesh.material) ? mesh.material : [mesh.material]
  materials.forEach(material => {
    material.color.setHex(material.userData.originalColor ?? 0x94a3b8)
    material.emissive?.setHex(material.userData.originalEmissive ?? 0x000000)
    material.emissiveIntensity = 0
    material.transparent = Boolean(material.userData.originalTransparent)
    material.opacity = material.userData.originalOpacity ?? 1
    material.needsUpdate = true
  })
}

const descendantKeys = node => {
  const nodes = flattenProgressNodes([node])
  return new Set(nodes.map(item => item.meshKey).filter(Boolean))
}

const applyProgressStyles = () => {
  if (!modelGroup) return
  const nodes = flattenProgressNodes(props.progressNodes)
  const nodesByMeshKey = new Map(nodes.filter(node => node.meshKey).map(node => [node.meshKey, node]))
  const selectedNode = nodes.find(node => node.id === props.selectedNodeId)
  const isolatedKeys = props.isolateSelected && selectedNode ? descendantKeys(selectedNode) : null
  const boundIds = new Set()
  const boundMeshKeys = new Set()
  const bounds = new THREE.Box3().setFromObject(modelGroup)
  clipPlane.constant = THREE.MathUtils.lerp(bounds.min.x, bounds.max.x, props.clipPosition)

  modelGroup.traverse(mesh => {
    if (!mesh.isMesh) return
    mesh.visible = true
    resetMesh(mesh)
    const progressKey = progressKeyFor(mesh, nodesByMeshKey)
    const node = nodesByMeshKey.get(progressKey)
    const clippingPlanes = props.clipEnabled ? [clipPlane] : null
    const meshMaterials = Array.isArray(mesh.material) ? mesh.material : [mesh.material]
    meshMaterials.forEach(material => {
      material.clippingPlanes = clippingPlanes
      material.clipShadows = true
    })
    if (!node) return
    boundIds.add(node.id)
    boundMeshKeys.add(progressKey)
    setMeshStatus(mesh, node.displayStatus || node.status, node.id === props.selectedNodeId)
    if (props.statusFilter && node.displayStatus !== props.statusFilter) {
      mesh.visible = false
    }
    if (isolatedKeys && !isolatedKeys.has(node.meshKey)) {
      mesh.visible = false
    }
  })

  const unboundMembers = nodes
    .filter(node => node.nodeType === 'member' && node.meshKey && !boundMeshKeys.has(node.meshKey))
    .map(node => node.name)
  let modelMeshCount = 0
  modelGroup.traverse(item => {
    if (item.isMesh) modelMeshCount += 1
  })
  emit('binding-report', {
    boundCount: boundIds.size,
    unboundMembers,
    modelMeshCount
  })
}

const applyExplosion = () => {
  if (!modelGroup) return
  const segmented = modelGroup.children.find(child => child.name === 'Segmented-Bridge-System')
  if (!segmented) return
  segmented.children.forEach(group => {
    if (!group.userData.basePosition) {
      group.userData.basePosition = group.position.clone()
    }
    const offset = props.explode
      ? group.name.endsWith('deck') ? 7 : group.name.endsWith('girder') ? 3.5 : 0
      : 0
    group.position.copy(group.userData.basePosition).add(new THREE.Vector3(0, offset, 0))
  })
}

const loadModel = async source => {
  if (!renderer || !source) return
  clearModel()
  loading.value = true
  errorMessage.value = ''
  try {
    const gltf = await loader.loadAsync(source)
    clearModel()
    modelGroup = gltf.scene
    modelGroup.traverse(item => {
      if (item.isMesh) {
        prepareMaterials(item)
        item.frustumCulled = false
        item.castShadow = true
        item.receiveShadow = true
      }
    })
    scene.add(modelGroup)
    const bounds = new THREE.Box3().setFromObject(modelGroup)
    const gridSize = Math.max(bounds.getSize(new THREE.Vector3()).length() * 0.9, 8)
    gridHelper = new THREE.GridHelper(gridSize, 24, 0x2563eb, 0x1e293b)
    gridHelper.material.opacity = 0.25
    gridHelper.material.transparent = true
    gridHelper.position.y = bounds.min.y
    scene.add(gridHelper)
    fitCamera()
    applyExplosion()
    applyProgressStyles()
    focusNode(props.focusNodeId)
    emit('loaded')
  } catch (error) {
    clearModel()
    errorMessage.value = '请确认模型文件存在，且为未损坏的 GLB 格式。'
    emit('error', error)
  } finally {
    loading.value = false
  }
}

const render = () => {
  controls.update()
  renderer.render(scene, camera)
  animationId = requestAnimationFrame(render)
}

const resize = () => {
  const width = canvasRef.value?.clientWidth || 1
  const height = canvasRef.value?.clientHeight || 1
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}

const nodeFromMesh = mesh => {
  const nodes = flattenProgressNodes(props.progressNodes)
  const byKey = new Map(nodes.filter(node => node.meshKey).map(node => [node.meshKey, node]))
  const key = progressKeyFor(mesh, byKey)
  return key ? byKey.get(key) : null
}

const updatePointer = event => {
  const bounds = canvasRef.value.getBoundingClientRect()
  pointer.x = ((event.clientX - bounds.left) / bounds.width) * 2 - 1
  pointer.y = -((event.clientY - bounds.top) / bounds.height) * 2 + 1
}

const intersectNode = event => {
  if (!modelGroup || !canvasRef.value) return null
  updatePointer(event)
  raycaster.setFromCamera(pointer, camera)
  const hits = raycaster.intersectObject(modelGroup, true).filter(hit => hit.object.visible)
  return hits.length ? {mesh: hits[0].object, point: hits[0].point} : null
}

const handlePointerMove = event => {
  const hit = intersectNode(event)
  hoverNode.value = hit ? nodeFromMesh(hit.mesh) : null
  if (hoverNode.value) {
    const bounds = canvasRef.value.getBoundingClientRect()
    tooltipPosition.x = Math.min(event.clientX - bounds.left + 12, bounds.width - 150)
    tooltipPosition.y = Math.max(event.clientY - bounds.top - 62, 8)
  }
}

const rememberPointer = event => {
  pointerDown.x = event.clientX
  pointerDown.y = event.clientY
}

const handleCanvasClick = event => {
  const moved = Math.abs(event.clientX - pointerDown.x) + Math.abs(event.clientY - pointerDown.y)
  if (moved > 6) return
  const hit = intersectNode(event)
  const node = hit ? nodeFromMesh(hit.mesh) : null
  if (node) emit('select', node.id)
}

const focusNode = nodeId => {
  if (!modelGroup || !nodeId) return
  const node = flattenProgressNodes(props.progressNodes).find(item => item.id === nodeId)
  if (!node) return
  const keys = descendantKeys(node)
  const meshes = []
  modelGroup.traverse(mesh => {
    if (!mesh.isMesh) return
    const key = progressKeyFor(mesh, keys)
    if (keys.has(key)) meshes.push(mesh)
  })
  if (!meshes.length) return
  const bounds = new THREE.Box3()
  meshes.forEach(mesh => bounds.expandByObject(mesh))
  const size = new THREE.Vector3()
  const center = new THREE.Vector3()
  bounds.getSize(size)
  bounds.getCenter(center)
  const maxDimension = Math.max(size.x, size.y, size.z, 1)
  camera.position.copy(center).add(new THREE.Vector3(0.65, 0.42, 1).multiplyScalar(maxDimension * 2.1))
  camera.lookAt(center)
  controls.target.copy(center)
  controls.update()
}

const toggleExpanded = () => {
  expanded.value = !expanded.value
}

const handleKeydown = event => {
  if (event.key === 'Escape' && expanded.value) expanded.value = false
}

watch(
  () => [
    props.progressNodes,
    props.selectedNodeId,
    props.statusFilter,
    props.isolateSelected,
    props.clipEnabled,
    props.clipPosition
  ],
  () => applyProgressStyles(),
  {deep: true}
)

watch(() => props.explode, () => applyExplosion())

watch(() => props.focusNodeId, value => focusNode(value))

watch(() => props.src, value => loadModel(value))

onMounted(async () => {
  await nextTick()
  renderer = new THREE.WebGLRenderer({
    canvas: canvasRef.value,
    antialias: true,
    alpha: true,
    logarithmicDepthBuffer: true,
    preserveDrawingBuffer: true
  })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.localClippingEnabled = true
  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(45, 1, 0.1, 1000)
  camera.position.set(8, 6, 12)
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  scene.add(new THREE.HemisphereLight(0xffffff, 0x243b53, 2.2))
  const directionalLight = new THREE.DirectionalLight(0xffffff, 2.4)
  directionalLight.position.set(8, 12, 10)
  scene.add(directionalLight)
  const fillLight = new THREE.DirectionalLight(0x8ab4ff, 0.8)
  fillLight.position.set(-10, 6, -8)
  scene.add(fillLight)

  dracoLoader = new DRACOLoader()
  dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.7/')
  loader = new GLTFLoader()
  loader.setDRACOLoader(dracoLoader)

  resize()
  resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(canvasRef.value)
  document.addEventListener('keydown', handleKeydown)
  render()
  if (props.src) {
    await loadModel(props.src)
  }
})

onBeforeUnmount(() => {
  if (animationId) cancelAnimationFrame(animationId)
  document.removeEventListener('keydown', handleKeydown)
  resizeObserver?.disconnect()
  clearModel()
  controls?.dispose()
  dracoLoader?.dispose()
  renderer?.dispose()
  renderer?.forceContextLoss()
})
</script>

<style scoped lang="scss">
.model-viewer {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 420px;
  overflow: hidden;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: linear-gradient(180deg, #0b1220 0%, #101a2c 100%);
}

.model-canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.model-legend {
  position: absolute;
  left: 14px;
  bottom: 14px;
  z-index: 2;
  display: flex;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 6px;
  background: rgba(2, 6, 23, 0.74);
  color: #cbd5e1;
  font-size: 12px;
  backdrop-filter: blur(8px);
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;

  i {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.model-viewer--expanded {
  position: fixed;
  inset: 0;
  z-index: 2000;
  min-height: 0;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.model-view-controls {
  position: absolute;
  right: 14px;
  top: 14px;
  z-index: 2;
  display: flex;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 6px;
  background: rgba(2, 6, 23, 0.74);
  backdrop-filter: blur(8px);
}

.model-view-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #cbd5e1;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;

  &:hover {
    background: rgba(59, 130, 246, 0.22);
    color: #f8fafc;
  }

  &:active {
    background: rgba(59, 130, 246, 0.34);
  }
}

.model-tooltip {
  position: absolute;
  z-index: 3;
  display: grid;
  gap: 3px;
  min-width: 136px;
  padding: 8px 10px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 6px;
  background: rgba(2, 6, 23, 0.9);
  color: #cbd5e1;
  font-size: 12px;
  pointer-events: none;

  strong {
    color: #f8fafc;
  }
}

.model-state {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 2;
  transform: translate(-50%, -50%);
  color: #cbd5e1;
  font-size: 15px;
  pointer-events: none;
}

.model-state--error {
  width: min(360px, 90%);
  pointer-events: auto;
}

@media (max-width: 640px) {
  .model-legend {
    right: 14px;
    gap: 7px;
    padding: 7px 8px;
    font-size: 11px;
  }
}
</style>
