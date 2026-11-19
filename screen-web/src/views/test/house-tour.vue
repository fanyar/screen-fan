<template>
  <div class="house-tour">
    <canvas ref="canvasRef" class="tour-canvas"></canvas>

    <header class="tour-top">
      <button
        type="button"
        class="back-button"
        @click="returnToList"
      >
        返回
      </button>
      <button
        type="button"
        class="overview-button"
        :class="{ 'is-active': activeMode === 'overview' }"
        @click="showOverview"
      >
        全屋
      </button>
      <div class="title-block">
        <strong>{{ currentRoom?.name || '三室两厅' }}</strong>
        <span>{{ currentRoom ? `${currentRoom.name} · ${currentRoom.area}` : '户型模型 · 约109㎡' }}</span>
      </div>
    </header>

    <nav class="room-strip" aria-label="房间切换">
      <button
        v-for="room in rooms"
        :key="room.id"
        type="button"
        class="room-chip"
        :class="{ 'is-active': activeRoomId === room.id }"
        @click="enterRoom(room.id)"
      >
        {{ room.name }}
      </button>
    </nav>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { preservedListRouteQuery } from '@/util/list-query'

const canvasRef = ref(null)
const router = useRouter()
const route = useRoute()
const returnToList = () => router.push({
  path: '/platform/models',
  query: preservedListRouteQuery(route.query)
})
const activeMode = ref('overview')
const activeRoomId = ref('')

const rooms = reactive([
  { id: 'master', name: '主卧', area: '15.8㎡', bounds: [0, 0, 3.8, 4], view: [1.7, 1.55, 0.85, 0.18] },
  { id: 'bedroom2', name: '次卧', area: '13.6㎡', bounds: [3.8, 0, 7.2, 4], view: [5.4, 1.55, 0.85, 0.05] },
  { id: 'bedroom3', name: '书房', area: '17.1㎡', bounds: [7.2, 0, 11.8, 4], view: [9.6, 1.55, 0.85, -0.5] },
  { id: 'bathroom', name: '卫生间', area: '5.7㎡', bounds: [0, 4, 3.2, 5.8], view: [1.1, 1.5, 4.15, 1.15] },
  { id: 'hall', name: '过道', area: '9.2㎡', bounds: [3.2, 4, 8.4, 5.8], view: [5.8, 1.52, 4.9, -Math.PI / 2] },
  { id: 'kitchen', name: '厨房', area: '6.2㎡', bounds: [8.4, 4, 11.8, 5.8], view: [10.0, 1.52, 4.4, -1.31] },
  { id: 'living', name: '客厅', area: '21.8㎡', bounds: [0, 5.8, 6.8, 9.2], view: [2.4, 1.52, 8.35, 0.92] },
  { id: 'dining', name: '餐厅', area: '16.5㎡', bounds: [6.8, 5.8, 11.8, 9.2], view: [10.2, 1.52, 8.4, -2.33] }
])

const links = [
  { from: 'master', to: 'hall', target: 'hall', position: [3.45, 3.78] },
  { from: 'bedroom2', to: 'hall', target: 'hall', position: [5.5, 3.78] },
  { from: 'bedroom3', to: 'hall', target: 'hall', position: [7.9, 3.78] },
  { from: 'bathroom', to: 'hall', target: 'hall', position: [3.05, 4.9] },
  { from: 'kitchen', to: 'hall', target: 'hall', position: [8.55, 4.9] },
  { from: 'hall', to: 'living', target: 'living', position: [5.8, 5.65] },
  { from: 'hall', to: 'dining', target: 'dining', position: [7.7, 5.65] },
  { from: 'living', to: 'hall', target: 'hall', position: [5.6, 5.95] },
  { from: 'dining', to: 'hall', target: 'hall', position: [7.6, 5.95] }
]

const currentRoom = computed(() => rooms.find(room => room.id === activeRoomId.value))

let renderer
let scene
let camera
let controls
let resizeObserver
let animationId
let houseGroup
let navGroup
let roomLabels = []
let raycaster
let pointer
let clock
let overviewTarget
let lookTarget
let yaw = 0
let pitch = 0
let isTransitioning = false
let cameraAnimation = null
let pointerState = null

const INTERIOR_COLORS = {
  floor: '#e8e2d8',
  wetFloor: '#ccd7d2',
  wall: '#f6f4ef',
  outerWall: '#dfe3e8',
  trim: '#334155',
  wood: '#b98d68',
  darkWood: '#8a6a52',
  fabric: '#7f9caa',
  accent: '#5d7d68',
  metal: '#96a3ae',
  white: '#eceff2'
}

function box(parent, x, y, z, width, height, depth, color, options = {}) {
  const geometry = new THREE.BoxGeometry(width, height, depth)
  const material = new THREE.MeshStandardMaterial({
    color,
    roughness: options.roughness ?? 0.78,
    metalness: options.metalness ?? 0.02,
    transparent: options.transparent ?? false,
    opacity: options.opacity ?? 1
  })
  const mesh = new THREE.Mesh(geometry, material)
  mesh.position.set(x, y, z)
  parent.add(mesh)
  return mesh
}

function cylinder(parent, x, y, z, radiusTop, radiusBottom, height, color, segments = 20) {
  const geometry = new THREE.CylinderGeometry(radiusTop, radiusBottom, height, segments)
  const material = new THREE.MeshStandardMaterial({ color, roughness: 0.7 })
  const mesh = new THREE.Mesh(geometry, material)
  mesh.position.set(x, y, z)
  parent.add(mesh)
  return mesh
}

function makeLabel(text, subText, scale = 0.011) {
  const canvas = document.createElement('canvas')
  canvas.width = 360
  canvas.height = 140
  const context = canvas.getContext('2d')
  context.fillStyle = 'rgba(255,255,255,0.9)'
  context.roundRect?.(18, 20, 324, 100, 18)
  context.fill()
  context.strokeStyle = 'rgba(51,65,85,0.2)'
  context.lineWidth = 3
  context.roundRect?.(18, 20, 324, 100, 18)
  context.stroke()
  context.fillStyle = '#1e293b'
  context.font = '500 42px "PingFang SC", "Microsoft YaHei", sans-serif'
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  context.fillText(text, 180, subText ? 60 : 70)
  if (subText) {
    context.fillStyle = '#64748b'
    context.font = '28px "PingFang SC", "Microsoft YaHei", sans-serif'
    context.fillText(subText, 180, 100)
  }

  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  texture.anisotropy = 4
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: texture, transparent: true }))
  sprite.scale.set(14 * scale, 5.45 * scale, 1)
  sprite.renderOrder = 3
  return sprite
}

function createWall(start, end, openings = []) {
  const horizontal = Math.abs(end[1] - start[1]) < 0.01
  const distance = horizontal ? Math.abs(end[0] - start[0]) : Math.abs(end[1] - start[1])
  const ordered = [...openings].sort((a, b) => a.at - b.at)
  let cursor = 0

  const addSegment = (from, to, y, height) => {
    if (to - from < 0.015) return
    const center = (from + to) / 2
    const x = horizontal ? start[0] + center : start[0]
    const z = horizontal ? start[1] : start[1] + center
    const width = horizontal ? to - from : 0.14
    const depth = horizontal ? 0.14 : to - from
    box(houseGroup, x, y + height / 2, z, width, height, depth, INTERIOR_COLORS.wall)
  }

  ordered.forEach(opening => {
    const half = opening.width / 2
    addSegment(cursor, opening.at - half, 0, 2.7)
    if (opening.type === 'door') {
      addSegment(opening.at - half, opening.at + half, 2.12, 0.58)
    } else {
      addSegment(opening.at - half, opening.at + half, 0, 0.8)
      addSegment(opening.at - half, opening.at + half, 2.2, 0.5)
    }
    cursor = opening.at + half
  })
  addSegment(cursor, distance, 0, 2.7)

  const capX = horizontal ? start[0] + distance / 2 : start[0]
  const capZ = horizontal ? start[1] : start[1] + distance / 2
  box(
    houseGroup,
    capX,
    2.735,
    capZ,
    horizontal ? distance : 0.18,
    0.07,
    horizontal ? 0.18 : distance,
    INTERIOR_COLORS.trim
  )
}

function createFloors() {
  const base = box(houseGroup, 5.9, -0.09, 4.6, 12.9, 0.18, 10.3, '#c8ccd2')
  base.receiveShadow = false

  rooms.forEach(room => {
    const [x1, z1, x2, z2] = room.bounds
    const wet = room.id === 'bathroom' || room.id === 'kitchen'
    box(
      houseGroup,
      (x1 + x2) / 2,
      0.05,
      (z1 + z2) / 2,
      x2 - x1,
      0.1,
      z2 - z1,
      wet ? INTERIOR_COLORS.wetFloor : INTERIOR_COLORS.floor,
      { roughness: 0.9 }
    )
    const label = makeLabel(room.name, room.area, room.id === 'bathroom' ? 0.115 : 0.14)
    label.userData.isRoomLabel = true
    label.position.set((x1 + x2) / 2, 1.38, (z1 + z2) / 2)
    houseGroup.add(label)
    roomLabels.push(label)
  })
}

function createFurniture() {
  const master = houseGroup
  box(master, 1.6, 0.32, 1.15, 2.1, 0.44, 1.75, INTERIOR_COLORS.wood)
  box(master, 1.6, 0.62, 0.45, 2.05, 0.2, 0.24, INTERIOR_COLORS.white)
  box(master, 0.42, 0.28, 1.15, 0.45, 0.42, 0.42, INTERIOR_COLORS.darkWood)
  box(master, 2.8, 0.28, 1.15, 0.45, 0.42, 0.42, INTERIOR_COLORS.darkWood)
  box(master, 3.45, 0.8, 2.1, 0.42, 1.5, 1.6, INTERIOR_COLORS.darkWood)

  const bed2 = houseGroup
  box(bed2, 5.2, 0.28, 1.0, 1.85, 0.4, 1.45, INTERIOR_COLORS.wood)
  box(bed2, 5.2, 0.55, 0.42, 1.8, 0.18, 0.2, INTERIOR_COLORS.white)
  box(bed2, 6.9, 0.66, 2.0, 0.36, 1.25, 1.2, INTERIOR_COLORS.wood)
  box(bed2, 4.2, 0.58, 3.2, 1.0, 0.08, 0.42, INTERIOR_COLORS.fabric)

  const bed3 = houseGroup
  box(bed3, 8.8, 0.3, 1.05, 2.0, 0.42, 1.5, INTERIOR_COLORS.wood)
  box(bed3, 10.6, 0.72, 2.5, 1.2, 0.08, 0.5, INTERIOR_COLORS.fabric)
  box(bed3, 11.3, 0.72, 1.0, 0.6, 1.28, 1.2, INTERIOR_COLORS.darkWood)
  cylinder(bed3, 9.6, 0.33, 2.8, 0.36, 0.36, 0.55, INTERIOR_COLORS.accent)

  const bath = houseGroup
  box(bath, 0.4, 0.42, 4.95, 0.55, 0.45, 1.2, INTERIOR_COLORS.white)
  box(bath, 1.15, 0.55, 5.45, 1.1, 0.1, 0.55, INTERIOR_COLORS.white)
  cylinder(bath, 2.5, 0.28, 4.5, 0.42, 0.36, 0.5, INTERIOR_COLORS.white)

  const hall = houseGroup
  box(hall, 4.0, 0.38, 5.45, 0.75, 0.78, 0.28, INTERIOR_COLORS.white)
  box(hall, 4.0, 0.8, 5.45, 0.7, 0.06, 0.24, INTERIOR_COLORS.accent)

  const kitchen = houseGroup
  box(kitchen, 11.2, 0.45, 4.9, 0.85, 0.72, 2.0, INTERIOR_COLORS.white)
  box(kitchen, 10.6, 0.84, 4.9, 0.3, 0.08, 1.8, INTERIOR_COLORS.metal)
  box(kitchen, 11.2, 1.65, 4.9, 0.7, 0.8, 1.8, INTERIOR_COLORS.white)
  box(kitchen, 9.6, 0.45, 5.45, 1.2, 0.72, 0.6, INTERIOR_COLORS.white)

  const living = houseGroup
  box(living, 0.65, 0.28, 7.6, 0.85, 0.56, 2.5, INTERIOR_COLORS.fabric)
  box(living, 1.45, 0.22, 7.6, 0.5, 0.36, 2.3, INTERIOR_COLORS.darkWood)
  box(living, 2.6, 0.2, 7.6, 0.85, 0.24, 1.3, INTERIOR_COLORS.wood)
  box(living, 4.7, 0.28, 7.5, 0.4, 0.55, 1.6, INTERIOR_COLORS.white)
  box(living, 4.7, 1.35, 7.5, 0.14, 0.85, 1.25, '#243038')
  cylinder(living, 3.4, 0.25, 8.6, 0.34, 0.34, 0.42, INTERIOR_COLORS.accent)

  const dining = houseGroup
  box(dining, 9.4, 0.42, 7.5, 1.8, 0.08, 1.0, INTERIOR_COLORS.wood)
  box(dining, 9.4, 0.22, 7.5, 1.3, 0.36, 0.6, INTERIOR_COLORS.darkWood)
  box(dining, 8.55, 0.52, 7.5, 0.42, 0.82, 0.42, INTERIOR_COLORS.fabric)
  box(dining, 10.25, 0.52, 7.5, 0.42, 0.82, 0.42, INTERIOR_COLORS.fabric)
  box(dining, 11.3, 0.42, 8.65, 0.7, 0.75, 0.35, INTERIOR_COLORS.white)
}

function createHouse() {
  houseGroup = new THREE.Group()
  createFloors()

  createWall([0, 0], [11.8, 0], [
    { at: 1.8, width: 1.6, type: 'window' },
    { at: 5.5, width: 1.5, type: 'window' },
    { at: 9.5, width: 1.7, type: 'window' }
  ])
  createWall([0, 9.2], [11.8, 9.2], [
    { at: 3.0, width: 2.0, type: 'window' },
    { at: 9.2, width: 2.0, type: 'window' }
  ])
  createWall([0, 0], [0, 9.2], [
    { at: 2.0, width: 1.5, type: 'window' },
    { at: 7.5, width: 2.1, type: 'window' }
  ])
  createWall([11.8, 0], [11.8, 9.2], [
    { at: 2.0, width: 1.4, type: 'window' },
    { at: 5.0, width: 1.4, type: 'window' },
    { at: 7.8, width: 0.95, type: 'door' }
  ])

  createWall([0, 4], [11.8, 4], [
    { at: 3.45, width: 0.88, type: 'door' },
    { at: 5.5, width: 0.88, type: 'door' },
    { at: 7.9, width: 0.88, type: 'door' }
  ])
  createWall([0, 5.8], [3.2, 5.8])
  createWall([8.4, 5.8], [11.8, 5.8])
  createWall([3.2, 4], [3.2, 5.8], [{ at: 0.9, width: 0.8, type: 'door' }])
  createWall([8.4, 4], [8.4, 5.8], [{ at: 0.9, width: 0.8, type: 'door' }])
  createWall([3.8, 0], [3.8, 4])
  createWall([7.2, 0], [7.2, 4])
  createWall([6.8, 5.8], [6.8, 7.0])
  createWall([6.8, 8.8], [6.8, 9.2])
  createWall([7.0, 5.8], [11.8, 5.8], [{ at: 2.0, width: 1.8, type: 'window' }])

  scene.add(houseGroup)
  createFurniture()
}

function createNavMarker(link) {
  const group = new THREE.Group()
  group.position.set(link.position[0], 0.08, link.position[1])
  group.userData = { targetRoom: link.target, isNavMarker: true }

  const ring = new THREE.Mesh(
    new THREE.RingGeometry(0.16, 0.24, 40),
    new THREE.MeshStandardMaterial({ color: '#2563eb', transparent: true, opacity: 0.9, side: THREE.DoubleSide })
  )
  ring.rotation.x = -Math.PI / 2
  ring.position.y = 0.01
  group.add(ring)

  const cone = new THREE.Mesh(
    new THREE.ConeGeometry(0.11, 0.24, 24),
    new THREE.MeshStandardMaterial({ color: '#2563eb', roughness: 0.35 })
  )
  cone.position.y = 0.34
  group.add(cone)

  const targetRoom = rooms.find(room => room.id === link.target)
  const label = makeLabel(targetRoom.name, '', 0.04)
  label.position.y = 0.74
  group.add(label)

  group.traverse(child => {
    child.userData = { targetRoom: link.target, isNavMarker: true }
  })
  return group
}

function createNavMarkers() {
  navGroup = new THREE.Group()
  navGroup.visible = false
  links.forEach(link => {
    navGroup.add(createNavMarker(link))
  })
  scene.add(navGroup)
}

function roomTarget(room) {
  const [x, , z, initialYaw] = room.view
  return new THREE.Vector3(x + Math.sin(initialYaw) * 5, 1.35, z + Math.cos(initialYaw) * 5)
}

function updateLookTarget() {
  lookTarget.set(
    camera.position.x + Math.sin(yaw) * Math.cos(pitch) * 5,
    camera.position.y + Math.sin(pitch) * 5,
    camera.position.z + Math.cos(yaw) * Math.cos(pitch) * 5
  )
  camera.lookAt(lookTarget)
}

function beginRoomLook(room) {
  const [x, y, z, initialYaw] = room.view
  camera.position.set(x, y, z)
  yaw = initialYaw
  pitch = -0.05
  updateLookTarget()
}

function animateCamera(endPosition, endTarget, endMode, roomId = '') {
  if (cameraAnimation) cancelAnimationFrame(cameraAnimation)
  const startPosition = camera.position.clone()
  const startTarget = activeMode.value === 'overview' || isTransitioning
    ? controls.target.clone()
    : lookTarget.clone()
  const startTime = performance.now()
  const distance = startPosition.distanceTo(endPosition)
  const duration = Math.min(1500, Math.max(700, distance * 75))
  const arc = activeMode.value === 'room' && endMode === 'room' ? Math.min(3.2, distance * 0.22) : 1.0
  isTransitioning = true
  activeMode.value = 'transition'
  controls.enabled = false

  const tick = now => {
    const linear = Math.min(1, (now - startTime) / duration)
    const eased = linear < 0.5 ? 2 * linear * linear : 1 - Math.pow(-2 * linear + 2, 2) / 2
    camera.position.lerpVectors(startPosition, endPosition, eased)
    camera.position.y += Math.sin(linear * Math.PI) * arc
    lookTarget.lerpVectors(startTarget, endTarget, eased)
    camera.lookAt(lookTarget)

    if (linear < 1) {
      cameraAnimation = requestAnimationFrame(tick)
      return
    }

    cameraAnimation = null
    activeMode.value = endMode
    activeRoomId.value = roomId
    isTransitioning = false
    if (endMode === 'overview') {
      controls.target.copy(endTarget)
      controls.enabled = true
      controls.update()
    } else {
      beginRoomLook(rooms.find(room => room.id === roomId))
    }
  }
  cameraAnimation = requestAnimationFrame(tick)
}

function enterRoom(roomId) {
  if (isTransitioning) return
  const room = rooms.find(item => item.id === roomId)
  if (!room) return
  const [x, y, z] = room.view
  beginRoomLook(room)
  const endTarget = lookTarget.clone()
  animateCamera(new THREE.Vector3(x, y, z), endTarget, 'room', roomId)
}

function showOverview() {
  if (isTransitioning) return
  controls.target.copy(overviewTarget)
  const direction = new THREE.Vector3(6.8, 16.5, 9.6)
  animateCamera(overviewTarget.clone().add(direction), overviewTarget, 'overview')
}

function resize() {
  const element = canvasRef.value
  if (!element || !renderer) return
  const width = element.clientWidth || window.innerWidth
  const height = element.clientHeight || window.innerHeight
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}

function handlePointerDown(event) {
  pointerState = {
    x: event.clientX,
    y: event.clientY,
    time: performance.now(),
    moved: false
  }
}

function handlePointerMove(event) {
  if (!pointerState || activeMode.value !== 'room') return
  const dx = event.clientX - pointerState.x
  const dy = event.clientY - pointerState.y
  if (Math.abs(dx) + Math.abs(dy) > 2) pointerState.moved = true
  yaw -= dx * 0.006
  pitch = THREE.MathUtils.clamp(pitch - dy * 0.005, -0.95, 0.8)
  pointerState.x = event.clientX
  pointerState.y = event.clientY
  updateLookTarget()
}

function selectMarker(object) {
  let current = object
  while (current && !current.userData.isNavMarker) current = current.parent
  if (current?.userData.targetRoom) enterRoom(current.userData.targetRoom)
}

function handlePointerUp(event) {
  if (!pointerState) return
  const elapsed = performance.now() - pointerState.time
  const moved = Math.hypot(event.clientX - pointerState.x, event.clientY - pointerState.y) > 5 || pointerState.moved
  pointerState = null
  if (moved || elapsed > 500 || activeMode.value !== 'room') return

  const bounds = canvasRef.value.getBoundingClientRect()
  pointer.set(((event.clientX - bounds.left) / bounds.width) * 2 - 1, -((event.clientY - bounds.top) / bounds.height) * 2 + 1)
  raycaster.setFromCamera(pointer, camera)
  const hits = raycaster.intersectObjects(navGroup.children, true)
  if (hits.length) selectMarker(hits[0].object)
}

function renderFrame() {
  animationId = requestAnimationFrame(renderFrame)
  const elapsed = clock.getElapsedTime()
  navGroup.visible = activeMode.value === 'room'
  roomLabels.forEach(label => {
    label.visible = activeMode.value !== 'room'
  })
  if (navGroup.visible) {
    navGroup.children.forEach((group, index) => {
      group.children[1].position.y = 0.34 + Math.sin(elapsed * 2.6 + index * 1.3) * 0.035
    })
  }
  if (activeMode.value === 'overview' && controls.enabled) controls.update()
  renderer.render(scene, camera)
}

function disposeObject(object) {
  object.traverse(child => {
    child.geometry?.dispose()
    const materials = Array.isArray(child.material) ? child.material : [child.material]
    materials.forEach(material => {
      material?.map?.dispose()
      material?.dispose()
    })
  })
}

onMounted(async () => {
  await nextTick()
  renderer = new THREE.WebGLRenderer({
    canvas: canvasRef.value,
    antialias: true,
    alpha: true
  })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.05

  scene = new THREE.Scene()
  scene.background = new THREE.Color('#e4e8ee')
  camera = new THREE.PerspectiveCamera(63, 1, 0.05, 240)
  overviewTarget = new THREE.Vector3(5.9, 0.5, 4.6)
  lookTarget = new THREE.Vector3()
  camera.position.copy(overviewTarget).add(new THREE.Vector3(6.8, 16.5, 9.6))
  camera.lookAt(overviewTarget)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.copy(overviewTarget)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.minDistance = 8
  controls.maxDistance = 28
  controls.maxPolarAngle = Math.PI / 2.35

  scene.add(new THREE.HemisphereLight('#ffffff', '#aab2bd', 2.4))
  const sunlight = new THREE.DirectionalLight('#fff7e8', 2.1)
  sunlight.position.set(10, 16, 13)
  scene.add(sunlight)
  const fillLight = new THREE.DirectionalLight('#dfeaf7', 0.9)
  fillLight.position.set(-8, 8, -10)
  scene.add(fillLight)

  raycaster = new THREE.Raycaster()
  pointer = new THREE.Vector2()
  clock = new THREE.Clock()
  createHouse()
  createNavMarkers()

  resize()
  resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(canvasRef.value)
  renderer.domElement.addEventListener('pointerdown', handlePointerDown)
  renderer.domElement.addEventListener('pointermove', handlePointerMove)
  renderer.domElement.addEventListener('pointerup', handlePointerUp)
  renderer.domElement.addEventListener('contextmenu', event => event.preventDefault())
  renderFrame()
})

onBeforeUnmount(() => {
  if (cameraAnimation) cancelAnimationFrame(cameraAnimation)
  if (animationId) cancelAnimationFrame(animationId)
  resizeObserver?.disconnect()
  renderer?.domElement.removeEventListener('pointerdown', handlePointerDown)
  renderer?.domElement.removeEventListener('pointermove', handlePointerMove)
  renderer?.domElement.removeEventListener('pointerup', handlePointerUp)
  controls?.dispose()
  if (houseGroup) disposeObject(houseGroup)
  if (navGroup) disposeObject(navGroup)
  renderer?.dispose()
  renderer?.forceContextLoss()
})
</script>

<style scoped lang="scss">
.house-tour {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: #e4e8ee;
  color: #172033;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.tour-canvas {
  display: block;
  width: 100%;
  height: 100%;
  touch-action: pan-y;
}

.tour-top {
  position: absolute;
  top: max(14px, env(safe-area-inset-top));
  left: 14px;
  right: 14px;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.back-button {
  flex: 0 0 auto;
  min-width: 58px;
  height: 38px;
  margin-right: -2px;
  padding: 0 12px;
  border: 1px solid rgba(23, 32, 51, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  color: #43506a;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}

.overview-button {
  margin-right: 2px;
}

.back-button,
.overview-button {
  flex: 0 0 auto;
  min-width: 58px;
  height: 38px;
  padding: 0 12px;
  border: 1px solid rgba(23, 32, 51, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  color: #43506a;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;

  &.is-active {
    background: #233248;
    border-color: #233248;
    color: #fff;
  }
}

.title-block {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  padding: 7px 11px;
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.58);
  backdrop-filter: blur(10px);

  strong {
    overflow: hidden;
    font-size: 15px;
    line-height: 20px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    overflow: hidden;
    color: #5c6678;
    font-size: 12px;
    line-height: 16px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.room-strip {
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: max(12px, env(safe-area-inset-bottom));
  z-index: 2;
  display: flex;
  gap: 7px;
  padding: 7px;
  overflow-x: auto;
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.74);
  backdrop-filter: blur(12px);
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.room-chip {
  flex: 0 0 auto;
  min-width: 52px;
  height: 34px;
  padding: 0 10px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #43506a;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;

  &.is-active {
    background: #233248;
    color: #fff;
  }
}

@media (max-width: 360px) {
  .back-button,
  .overview-button {
    min-width: 50px;
    padding: 0 9px;
    font-size: 13px;
  }

  .room-chip {
    min-width: 46px;
    padding: 0 8px;
    font-size: 12px;
  }
}
</style>
