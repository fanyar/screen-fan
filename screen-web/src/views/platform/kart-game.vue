<template>
  <div class="kart-game-page" @keydown.esc="togglePause">
    <div ref="viewportRef" class="game-viewport"></div>

    <!-- HUD -->
    <div v-show="gameState === 'racing' || gameState === 'countdown'" class="hud">
      <div class="hud-top">
        <div class="hud-chip">圈数 {{ lap }}/{{ TOTAL_LAPS }}</div>
        <div class="hud-chip hud-timer">{{ formatTime(elapsedTime) }}</div>
        <div class="hud-chip hud-best">最佳 {{ bestTime > 0 ? formatTime(bestTime) : '--:--' }}</div>
      </div>
      <div class="hud-speed">
        <span class="speed-value">{{ Math.round(displaySpeed) }}</span>
        <span class="speed-unit">km/h</span>
      </div>
      <div v-if="boostActive" class="boost-badge">BOOST!</div>
      <canvas ref="minimapRef" class="minimap" width="150" height="150"></canvas>
      <div class="hud-hint">ESC 暂停</div>
    </div>

    <!-- Start Screen -->
    <div v-if="gameState === 'ready'" class="overlay">
      <div class="overlay-panel">
        <h1 class="overlay-title">跑跑卡丁车</h1>
        <p class="overlay-sub">Three.js 3D 竞速 · 3 圈计时赛</p>
        <button class="btn-primary" @click="startRace">开始比赛</button>
        <div class="controls-grid">
          <div class="control-row"><kbd>↑</kbd><span>加速</span></div>
          <div class="control-row"><kbd>↓</kbd><span>刹车 / 倒车</span></div>
          <div class="control-row"><kbd>←</kbd><kbd>→</kbd><span>转向</span></div>
          <div class="control-row"><kbd>Shift</kbd><span>漂移</span></div>
          <div class="control-row"><kbd>ESC</kbd><span>暂停</span></div>
        </div>
      </div>
    </div>

    <!-- Countdown -->
    <div v-if="gameState === 'countdown'" class="overlay overlay--transparent">
      <span :key="countdownText" class="countdown-number">{{ countdownText }}</span>
    </div>

    <!-- Paused -->
    <div v-if="gameState === 'paused'" class="overlay">
      <div class="overlay-panel overlay-panel--compact">
        <h2 class="overlay-title overlay-title--sm">暂 停</h2>
        <div class="overlay-actions">
          <button class="btn-primary" @click="resumeRace">继续</button>
          <button class="btn-secondary" @click="restartRace">重新开始</button>
        </div>
      </div>
    </div>

    <!-- Finished -->
    <div v-if="gameState === 'finished'" class="overlay">
      <div class="overlay-panel overlay-panel--compact">
        <h2 class="overlay-title overlay-title--sm">比赛完成!</h2>
        <div class="result-time">{{ formatTime(elapsedTime) }}</div>
        <div v-if="isNewRecord" class="record-badge">新纪录!</div>
        <div v-else class="result-best">最佳成绩 {{ bestTime > 0 ? formatTime(bestTime) : '--' }}</div>
        <div class="overlay-actions">
          <button class="btn-primary" @click="restartRace">再来一局</button>
          <button class="btn-secondary" @click="$router.push('/platform/games')">返回</button>
        </div>
      </div>
    </div>

    <!-- Back button -->
    <button v-if="gameState !== 'ready'" class="back-btn" @click="$router.push('/platform/games')">
      ← 返回游戏列表
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const TOTAL_LAPS = 3
const TRACK_WIDTH = 14
const MAX_SPEED = 40
const ACCEL = 28
const BRAKE = 40
const REVERSE_MAX = -12
const BASE_SPEED = 18
const RETURN_DECEL = 9
const TO_BASE_ACCEL = 14
const FRICTION = 0.995
const BASE_STEER = 1.6
const DRIFT_STEER = 2.4
const CAMERA_DIST = 9
const CAMERA_HEIGHT = 4.5
const BOOST_PAD_SPEED = 55
const BEST_TIME_KEY = 'kart-best-time'

// ── refs ────────────────────────────────────────────────
const viewportRef = ref(null)
const minimapRef = ref(null)
const gameState = ref('ready')
const lap = ref(1)
const elapsedTime = ref(0)
const currentSpeed = ref(0)
const displaySpeed = ref(0)
const boostActive = ref(false)
const countdownText = ref('3')
const isNewRecord = ref(false)
const bestTime = ref(0)

// ── track definition ────────────────────────────────────
const TRACK_CONTROL_POINTS = [
  new THREE.Vector3(0, 0, -800),
  new THREE.Vector3(500, 0, -750),
  new THREE.Vector3(900, 0, -500),
  new THREE.Vector3(1000, 0, 0),
  new THREE.Vector3(900, 0, 550),
  new THREE.Vector3(500, 0, 850),
  new THREE.Vector3(-100, 0, 950),
  new THREE.Vector3(-700, 0, 850),
  new THREE.Vector3(-1050, 0, 450),
  new THREE.Vector3(-1100, 0, -100),
  new THREE.Vector3(-950, 0, -600),
  new THREE.Vector3(-500, 0, -850)
]

const trackCurve = new THREE.CatmullRomCurve3(TRACK_CONTROL_POINTS, true, 'catmullrom', 0.5)
const trackSamples = []
const SAMPLE_COUNT = 1500
for (let i = 0; i < SAMPLE_COUNT; i++) {
  const t = i / SAMPLE_COUNT
  const point = trackCurve.getPointAt(t)
  const tangent = trackCurve.getTangentAt(t)
  const normal = new THREE.Vector3(-tangent.z, 0, tangent.x).normalize()
  trackSamples.push({ point, tangent, normal, t })
}

// boost pads: [curveT, offsetFromCenter]
const BOOST_PADS = [
  { t: 0.05, offset: 0 },
  { t: 0.14, offset: 2 },
  { t: 0.23, offset: -2 },
  { t: 0.32, offset: 0 },
  { t: 0.41, offset: 2 },
  { t: 0.5, offset: 0 },
  { t: 0.59, offset: -2 },
  { t: 0.68, offset: 0 },
  { t: 0.77, offset: 2 },
  { t: 0.86, offset: 0 },
  { t: 0.94, offset: -2 }
]

// ── game state (non-reactive for performance) ──────────
let renderer, scene, camera, clock
let kartGroup, kartBody
let sunLight
let animationId = null
let isRendererInitialized = false

const kart = {
  x: 0, z: -55,
  heading: 0,
  speed: 0,
  lateral: 0,
  drifting: false,
  boostTimer: 0,
  curveT: 0,
  prevSector: 0,
  sectorProgress: 0,
  wheels: []
}

const keys = { forward: false, backward: false, left: false, right: false, drift: false }

// ── helpers ─────────────────────────────────────────────
function formatTime(seconds) {
  if (seconds <= 0) return '--:--.--'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  const ms = Math.floor((seconds % 1) * 100)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}.${String(ms).padStart(2, '0')}`
}

function loadBestTime() {
  try {
    return parseFloat(localStorage.getItem(BEST_TIME_KEY)) || 0
  } catch { return 0 }
}

function saveBestTime(t) {
  try { localStorage.setItem(BEST_TIME_KEY, String(t)) } catch { /* ignore */ }
}

// ── scene init ──────────────────────────────────────────
function initScene() {
  const container = viewportRef.value
  const w = container.clientWidth
  const h = container.clientHeight

  renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x87ceeb)
  scene.fog = new THREE.Fog(0x87ceeb, 150, 1200)

  camera = new THREE.PerspectiveCamera(60, w / h, 0.1, 6000)
  camera.position.set(0, CAMERA_HEIGHT, -800 + CAMERA_DIST)

  clock = new THREE.Clock()

  // Lights
  sunLight = new THREE.DirectionalLight(0xffffff, 1.2)
  sunLight.position.set(50, 80, 30)
  sunLight.castShadow = true
  sunLight.shadow.mapSize.set(2048, 2048)
  sunLight.shadow.camera.left = -120
  sunLight.shadow.camera.right = 120
  sunLight.shadow.camera.top = 120
  sunLight.shadow.camera.bottom = -120
  sunLight.shadow.camera.near = 10
  sunLight.shadow.camera.far = 400
  scene.add(sunLight)
  scene.add(sunLight.target)
  scene.add(new THREE.HemisphereLight(0x87ceeb, 0x3a7d44, 0.6))

  buildGround()
  buildTrackMesh()
  buildStartLine()
  buildBoostPads()
  buildEnvironment()
  buildKart()

  isRendererInitialized = true
}

function buildGround() {
  const geo = new THREE.PlaneGeometry(4000, 4000)
  const mat = new THREE.MeshStandardMaterial({ color: 0x3a7d44, roughness: 1 })
  const ground = new THREE.Mesh(geo, mat)
  ground.rotation.x = -Math.PI / 2
  ground.position.y = -0.05
  ground.receiveShadow = true
  scene.add(ground)
}

function buildTrackMesh() {
  const halfW = TRACK_WIDTH / 2
  const positions = []
  const uvs = []
  const indices = []

  for (let i = 0; i <= SAMPLE_COUNT; i++) {
    const s = trackSamples[i % SAMPLE_COUNT]
    const lx = s.point.x + s.normal.x * halfW
    const lz = s.point.z + s.normal.z * halfW
    const rx = s.point.x - s.normal.x * halfW
    const rz = s.point.z - s.normal.z * halfW
    positions.push(lx, 0.02, lz, rx, 0.02, rz)
    uvs.push(0, i * 0.3, 1, i * 0.3)
  }
  for (let i = 0; i < SAMPLE_COUNT; i++) {
    const a = i * 2, b = a + 1, c = (i + 1) * 2, d = c + 1
    indices.push(a, c, b, b, c, d)
  }

  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  geo.setAttribute('uv', new THREE.Float32BufferAttribute(uvs, 2))
  geo.setIndex(indices)
  geo.computeVertexNormals()

  const mat = new THREE.MeshStandardMaterial({ color: 0x4a4a5a, roughness: 0.9 })
  const trackMesh = new THREE.Mesh(geo, mat)
  trackMesh.receiveShadow = true
  scene.add(trackMesh)

  // Edge lines
  for (const side of [1, -1]) {
    const edgePositions = []
    const edgeIndices = []
    for (let i = 0; i <= SAMPLE_COUNT; i++) {
      const s = trackSamples[i % SAMPLE_COUNT]
      const ox = s.point.x + s.normal.x * side * (halfW - 0.4)
      const oz = s.point.z + s.normal.z * side * (halfW - 0.4)
      const ix = s.point.x + s.normal.x * side * (halfW - 0.8)
      const iz = s.point.z + s.normal.z * side * (halfW - 0.8)
      edgePositions.push(ox, 0.05, oz, ix, 0.05, iz)
    }
    for (let i = 0; i < SAMPLE_COUNT; i++) {
      const a = i * 2, b = a + 1, c = (i + 1) * 2, d = c + 1
      edgeIndices.push(a, c, b, b, c, d)
    }
    const eGeo = new THREE.BufferGeometry()
    eGeo.setAttribute('position', new THREE.Float32BufferAttribute(edgePositions, 3))
    eGeo.setIndex(edgeIndices)
    eGeo.computeVertexNormals()
    const eMat = new THREE.MeshBasicMaterial({ color: 0xffffff })
    scene.add(new THREE.Mesh(eGeo, eMat))
  }
}

function buildStartLine() {
  const s = trackSamples[0]
  const geo = new THREE.PlaneGeometry(TRACK_WIDTH, 3)
  const colors = []
  const squares = 8
  for (let row = 0; row < 2; row++) {
    for (let col = 0; col < squares; col++) {
      colors.push((row + col) % 2 === 0 ? 0xffffff : 0x000000)
    }
  }
  // Simple checker texture
  const canvas = document.createElement('canvas')
  canvas.width = squares * 8
  canvas.height = 16
  const ctx = canvas.getContext('2d')
  for (let r = 0; r < 2; r++) {
    for (let c = 0; c < squares; c++) {
      ctx.fillStyle = (r + c) % 2 === 0 ? '#fff' : '#000'
      ctx.fillRect(c * 8, r * 8, 8, 8)
    }
  }
  const tex = new THREE.CanvasTexture(canvas)
  const mat = new THREE.MeshStandardMaterial({ map: tex, roughness: 0.8 })
  const mesh = new THREE.Mesh(geo, mat)
  mesh.position.copy(s.point)
  mesh.position.y = 0.04
  mesh.rotation.order = 'YXZ'
  mesh.rotation.x = -Math.PI / 2
  mesh.rotation.y = Math.atan2(s.tangent.x, s.tangent.z)
  scene.add(mesh)
}

function buildBoostPads() {
  // Bright chevron arrows with glow border
  const padCanvas = document.createElement('canvas')
  padCanvas.width = 64
  padCanvas.height = 128
  const pCtx = padCanvas.getContext('2d')
  pCtx.fillStyle = 'rgba(40, 20, 5, 0.92)'
  pCtx.fillRect(0, 0, 64, 128)
  pCtx.strokeStyle = '#ffcc00'
  pCtx.lineWidth = 5
  pCtx.strokeRect(3, 3, 58, 122)
  pCtx.fillStyle = '#ffcc00'
  for (let i = 0; i < 4; i++) {
    const y = i * 30 + 8
    pCtx.beginPath()
    pCtx.moveTo(10, y + 22)
    pCtx.lineTo(32, y)
    pCtx.lineTo(54, y + 22)
    pCtx.lineTo(54, y + 13)
    pCtx.lineTo(32, y + 9)
    pCtx.lineTo(10, y + 13)
    pCtx.closePath()
    pCtx.fill()
  }
  const padTex = new THREE.CanvasTexture(padCanvas)

  const mat = new THREE.MeshBasicMaterial({
    map: padTex,
  })
  BOOST_PADS.forEach(pad => {
    const idx = Math.floor(pad.t * SAMPLE_COUNT)
    const s = trackSamples[idx]
    const geo = new THREE.PlaneGeometry(8, 10)
    const mesh = new THREE.Mesh(geo, mat)
    mesh.position.set(
      s.point.x + s.normal.x * pad.offset,
      0.06,
      s.point.z + s.normal.z * pad.offset
    )
    mesh.rotation.order = 'YXZ'
    mesh.rotation.x = -Math.PI / 2
    mesh.rotation.y = Math.atan2(s.tangent.x, s.tangent.z)
    mesh.rotation.z = Math.PI
    scene.add(mesh)
  })
}

function buildEnvironment() {
  // Trees
  const trunkGeo = new THREE.CylinderGeometry(0.3, 0.4, 2, 8)
  const trunkMat = new THREE.MeshStandardMaterial({ color: 0x6b4423, roughness: 1 })
  const leafGeo = new THREE.ConeGeometry(1.5, 4, 8)
  const leafMat = new THREE.MeshStandardMaterial({ color: 0x2d6a4f, roughness: 1 })

  for (let i = 0; i < 60; i++) {
    const angle = (i / 60) * Math.PI * 2 + Math.random() * 0.3
    const dist = 1200 + Math.random() * 400
    const tx = Math.cos(angle) * dist
    const tz = Math.sin(angle) * dist

    const tree = new THREE.Group()
    const trunk = new THREE.Mesh(trunkGeo, trunkMat)
    trunk.position.y = 1
    trunk.castShadow = true
    tree.add(trunk)
    const leaf = new THREE.Mesh(leafGeo, leafMat)
    leaf.position.y = 3.5
    leaf.castShadow = true
    tree.add(leaf)
    tree.position.set(tx, 0, tz)
    tree.scale.set(2.5, 2.5, 2.5)
    scene.add(tree)
  }

  // Track-side barriers (red/white striped poles at corners)
  const poleGeo = new THREE.CylinderGeometry(0.15, 0.15, 1.2, 6)
  for (let i = 0; i < SAMPLE_COUNT; i += 25) {
    const s = trackSamples[i]
    for (const side of [1, -1]) {
      const px = s.point.x + s.normal.x * side * (TRACK_WIDTH / 2 + 1)
      const pz = s.point.z + s.normal.z * side * (TRACK_WIDTH / 2 + 1)
      const mat = new THREE.MeshStandardMaterial({
        color: i % 30 === 0 ? 0xff0000 : 0xffffff,
        roughness: 0.5
      })
      const pole = new THREE.Mesh(poleGeo, mat)
      pole.position.set(px, 0.6, pz)
      pole.castShadow = true
      scene.add(pole)
    }
  }
}

function buildKart() {
  kartGroup = new THREE.Group()

  // Body
  const bodyGeo = new THREE.BoxGeometry(1.2, 0.35, 2.0)
  const bodyMat = new THREE.MeshStandardMaterial({ color: 0xe63946, metalness: 0.3, roughness: 0.4 })
  kartBody = new THREE.Mesh(bodyGeo, bodyMat)
  kartBody.position.y = 0.4
  kartBody.castShadow = true
  kartGroup.add(kartBody)

  // Nose cone
  const noseGeo = new THREE.BoxGeometry(0.8, 0.22, 0.6)
  const noseMat = new THREE.MeshStandardMaterial({ color: 0xd62828, roughness: 0.4 })
  const nose = new THREE.Mesh(noseGeo, noseMat)
  nose.position.set(0, 0.35, 1.1)
  kartGroup.add(nose)

  // Seat
  const seatGeo = new THREE.BoxGeometry(0.6, 0.5, 0.5)
  const seatMat = new THREE.MeshStandardMaterial({ color: 0x2b2d42, roughness: 0.8 })
  const seat = new THREE.Mesh(seatGeo, seatMat)
  seat.position.set(0, 0.72, -0.05)
  kartGroup.add(seat)

  // Driver body
  const drvGeo = new THREE.BoxGeometry(0.4, 0.4, 0.3)
  const drvMat = new THREE.MeshStandardMaterial({ color: 0x457b9d, roughness: 0.7 })
  const driver = new THREE.Mesh(drvGeo, drvMat)
  driver.position.set(0, 0.95, 0.15)
  kartGroup.add(driver)

  // Helmet
  const helmetGeo = new THREE.SphereGeometry(0.22, 16, 16)
  const helmetMat = new THREE.MeshStandardMaterial({ color: 0xf1faee, roughness: 0.3 })
  const helmet = new THREE.Mesh(helmetGeo, helmetMat)
  helmet.position.set(0, 1.32, 0.15)
  kartGroup.add(helmet)

  // Visor
  const visorGeo = new THREE.BoxGeometry(0.3, 0.08, 0.06)
  const visorMat = new THREE.MeshStandardMaterial({ color: 0x1d3557, metalness: 0.6, roughness: 0.2 })
  const visor = new THREE.Mesh(visorGeo, visorMat)
  visor.position.set(0, 1.32, 0.3)
  kartGroup.add(visor)

  // Spoiler
  const spoilerGeo = new THREE.BoxGeometry(1.0, 0.06, 0.35)
  const spoilerMat = new THREE.MeshStandardMaterial({ color: 0x1d3557, roughness: 0.4 })
  const spoiler = new THREE.Mesh(spoilerGeo, spoilerMat)
  spoiler.position.set(0, 0.85, -0.95)
  kartGroup.add(spoiler)

  // Wheels
  const wheelGeo = new THREE.CylinderGeometry(0.3, 0.3, 0.25, 16)
  const wheelMat = new THREE.MeshStandardMaterial({ color: 0x1a1a2e, roughness: 0.9 })
  const rimGeo = new THREE.CylinderGeometry(0.15, 0.15, 0.27, 8)
  const rimMat = new THREE.MeshStandardMaterial({ color: 0xaaaaaa, metalness: 0.5, roughness: 0.4 })

  const wheelPos = [
    [-0.68, 0.3, 0.72],
    [0.68, 0.3, 0.72],
    [-0.68, 0.3, -0.72],
    [0.68, 0.3, -0.72]
  ]
  kart.wheels = []
  wheelPos.forEach(p => {
    const wheel = new THREE.Group()
    const tire = new THREE.Mesh(wheelGeo, wheelMat)
    tire.rotation.z = Math.PI / 2
    tire.castShadow = true
    wheel.add(tire)
    const rim = new THREE.Mesh(rimGeo, rimMat)
    rim.rotation.z = Math.PI / 2
    wheel.add(rim)
    wheel.position.set(...p)
    kartGroup.add(wheel)
    kart.wheels.push(wheel)
  })

  scene.add(kartGroup)
  updateKartTransform()
}

// ── game lifecycle ──────────────────────────────────────
function resetKart() {
  const s = trackSamples[0]
  lastNearestIdx = 0
  kart.x = s.point.x
  kart.z = s.point.z
  kart.heading = Math.atan2(s.tangent.x, s.tangent.z)
  kart.speed = 0
  kart.lateral = 0
  kart.drifting = false
  kart.boostTimer = 0
  kart.curveT = 0
  kart.prevSector = 0
  kart.sectorProgress = 0
  lap.value = 1
  elapsedTime.value = 0
  currentSpeed.value = 0
  displaySpeed.value = 0
  keys.forward = false
  keys.backward = false
  keys.left = false
  keys.right = false
  keys.drift = false
  updateKartTransform()
}

function startRace() {
  resetKart()
  gameState.value = 'countdown'
  countdownText.value = '3'
  let count = 3
  const timer = setInterval(() => {
    count--
    if (count > 0) {
      countdownText.value = String(count)
    } else if (count === 0) {
      countdownText.value = 'GO!'
    } else {
      clearInterval(timer)
      gameState.value = 'racing'
    }
  }, 1000)
}

function togglePause() {
  if (gameState.value === 'racing') {
    gameState.value = 'paused'
  } else if (gameState.value === 'paused') {
    resumeRace()
  }
}

function resumeRace() {
  gameState.value = 'racing'
  clock.getDelta() // consume accumulated delta
}

function restartRace() {
  resetKart()
  startRace()
}

function finishRace() {
  gameState.value = 'finished'
  const time = elapsedTime.value
  if (bestTime.value === 0 || time < bestTime.value) {
    bestTime.value = time
    saveBestTime(time)
    isNewRecord.value = true
  } else {
    isNewRecord.value = false
  }
}

// ── input ───────────────────────────────────────────────
function onKeyDown(e) {
  if (e.code === 'Escape') { togglePause(); return }
  // Always track key state so keys held before the race starts
  // (e.g. during countdown) take effect immediately
  switch (e.code) {
    case 'KeyW': case 'ArrowUp': keys.forward = true; e.preventDefault(); break
    case 'KeyS': case 'ArrowDown': keys.backward = true; e.preventDefault(); break
    case 'KeyA': case 'ArrowLeft': keys.left = true; e.preventDefault(); break
    case 'KeyD': case 'ArrowRight': keys.right = true; e.preventDefault(); break
    case 'ShiftLeft': case 'ShiftRight': keys.drift = true; break
    case 'Enter': if (gameState.value === 'ready') startRace(); break
  }
}

function onKeyUp(e) {
  switch (e.code) {
    case 'KeyW': case 'ArrowUp': keys.forward = false; break
    case 'KeyS': case 'ArrowDown': keys.backward = false; break
    case 'KeyA': case 'ArrowLeft': keys.left = false; break
    case 'KeyD': case 'ArrowRight': keys.right = false; break
    case 'ShiftLeft': case 'ShiftRight': keys.drift = false; break
  }
}

// ── physics ─────────────────────────────────────────────
let lastNearestIdx = 0
function findNearestTrackSample(x, z) {
  // Local search near the last known position to avoid jumping
  // across close track sections (e.g. S-curves)
  const window = 40
  let minDist = Infinity
  let nearest = lastNearestIdx
  for (let off = -window; off <= window; off++) {
    const i = (lastNearestIdx + off + SAMPLE_COUNT) % SAMPLE_COUNT
    const s = trackSamples[i]
    const dx = x - s.point.x
    const dz = z - s.point.z
    const d = dx * dx + dz * dz
    if (d < minDist) { minDist = d; nearest = i }
  }
  lastNearestIdx = nearest
  return nearest
}

function updatePhysics(dt) {
  const isDrifting = keys.drift && Math.abs(kart.speed) > 5

  // Acceleration / brake / auto-forward
  if (keys.forward) {
    kart.speed += ACCEL * dt
    kart.speed = Math.min(kart.speed, MAX_SPEED)
  } else if (keys.backward) {
    if (kart.speed > 0) {
      kart.speed -= BRAKE * dt
    } else {
      kart.speed = Math.max(kart.speed - ACCEL * 0.6 * dt, REVERSE_MAX)
    }
  } else {
    // No input: auto-cruise toward base speed
    if (kart.speed < BASE_SPEED) {
      kart.speed = Math.min(kart.speed + TO_BASE_ACCEL * dt, BASE_SPEED)
    } else if (kart.speed > BASE_SPEED) {
      kart.speed = Math.max(kart.speed - RETURN_DECEL * dt, BASE_SPEED)
    }
  }

  // Boost decay
  if (kart.boostTimer > 0) {
    kart.boostTimer -= dt
    if (kart.boostTimer > 0) {
      kart.speed = Math.max(kart.speed, BOOST_PAD_SPEED * Math.min(kart.boostTimer / 2, 1))
    }
  }

  // Steering
  const steerInput = (keys.left ? 1 : 0) + (keys.right ? -1 : 0)
  if (steerInput !== 0 && Math.abs(kart.speed) > 0.5) {
    const steerRate = isDrifting ? DRIFT_STEER : BASE_STEER
    const speedFactor = Math.min(Math.abs(kart.speed) / 15, 1)
    const dir = kart.speed >= 0 ? 1 : -1
    kart.heading += steerInput * steerRate * speedFactor * dt * dir
  }

  // Lateral drift
  if (isDrifting) {
    kart.lateral -= steerInput * 14 * dt * Math.min(Math.abs(kart.speed) / MAX_SPEED, 1)
  }
  const gripDamp = isDrifting ? 0.99 : 0.85
  kart.lateral *= gripDamp

  // Compose velocity
  const fx = Math.sin(kart.heading)
  const fz = Math.cos(kart.heading)
  const rx = Math.cos(kart.heading)
  const rz = -Math.sin(kart.heading)

  let vx = fx * kart.speed + rx * kart.lateral
  let vz = fz * kart.speed + rz * kart.lateral

  kart.x += vx * dt
  kart.z += vz * dt

  // Track boundary collision
  const nearestIdx = findNearestTrackSample(kart.x, kart.z)
  const nearest = trackSamples[nearestIdx]
  const dx = kart.x - nearest.point.x
  const dz = kart.z - nearest.point.z
  const lateralDist = dx * nearest.normal.x + dz * nearest.normal.z
  const halfW = TRACK_WIDTH / 2

  if (Math.abs(lateralDist) > halfW) {
    const push = Math.abs(lateralDist) - halfW
    const sign = Math.sign(lateralDist)
    kart.x -= nearest.normal.x * push * sign
    kart.z -= nearest.normal.z * push * sign
    kart.speed = Math.max(kart.speed * 0.9, BASE_SPEED * 0.6)
    kart.lateral *= 0.5
  }

  kart.curveT = nearest.t

  // Boost pad check
  for (const pad of BOOST_PADS) {
    const padIdx = Math.floor(pad.t * SAMPLE_COUNT)
    const ps = trackSamples[padIdx]
    const padCx = ps.point.x + ps.normal.x * pad.offset
    const padCz = ps.point.z + ps.normal.z * pad.offset
    const pdx = kart.x - padCx
    const pdz = kart.z - padCz
    // Match visual pad (3 wide, 6 long) with small margin
    const lateral = pdx * ps.normal.x + pdz * ps.normal.z
    const longitudinal = pdx * ps.tangent.x + pdz * ps.tangent.z
    if (Math.abs(lateral) < 2 && Math.abs(longitudinal) < 3.5) {
      kart.boostTimer = 2
    }
  }

  // Sector-based lap detection
  const sector = Math.floor(kart.curveT * 4) % 4
  if (sector !== kart.prevSector) {
    if (sector === (kart.prevSector + 1) % 4) {
      kart.sectorProgress++
    } else if (sector === (kart.prevSector + 3) % 4) {
      kart.sectorProgress = Math.max(0, kart.sectorProgress - 1)
    }
    kart.prevSector = sector
  }
  if (kart.sectorProgress >= 4) {
    kart.sectorProgress = 0
    lap.value++
    if (lap.value > TOTAL_LAPS) {
      finishRace()
    }
  }

  kart.drifting = isDrifting
  currentSpeed.value = kart.speed
  displaySpeed.value = Math.abs(kart.speed) * 3
  boostActive.value = kart.boostTimer > 0
}

function updateKartTransform() {
  kartGroup.position.set(kart.x, 0, kart.z)
  kartGroup.rotation.y = kart.heading - kart.lateral * 0.012
  kartGroup.rotation.z = -kart.lateral * 0.025
  // Spin wheels
  const spin = kart.speed * 0.1
  kart.wheels.forEach(w => { w.children[0].rotation.x += spin })
}

function updateCamera() {
  const targetX = kart.x - Math.sin(kart.heading) * CAMERA_DIST
  const targetZ = kart.z - Math.cos(kart.heading) * CAMERA_DIST
  const targetY = CAMERA_HEIGHT

  camera.position.x += (targetX - camera.position.x) * 0.08
  camera.position.y += (targetY - camera.position.y) * 0.08
  camera.position.z += (targetZ - camera.position.z) * 0.08

  const lookAheadX = kart.x + Math.sin(kart.heading) * 3
  const lookAheadZ = kart.z + Math.cos(kart.heading) * 3
  camera.lookAt(lookAheadX, 1, lookAheadZ)
}

// ── minimap ─────────────────────────────────────────────
let minimapBounds = null
let minimapCache = null
let mmToX = null
let mmToZ = null

function computeMinimapBounds() {
  let minX = Infinity, maxX = -Infinity, minZ = Infinity, maxZ = -Infinity
  for (const s of trackSamples) {
    minX = Math.min(minX, s.point.x)
    maxX = Math.max(maxX, s.point.x)
    minZ = Math.min(minZ, s.point.z)
    maxZ = Math.max(maxZ, s.point.z)
  }
  const pad = 8
  minX -= pad; maxX += pad; minZ -= pad; maxZ += pad
  minimapBounds = { minX, maxX, minZ, maxZ }

  // Pre-render the track outline once; per-frame we just blit it
  const size = 150
  const scale = Math.min(size / (maxX - minX), size / (maxZ - minZ))
  mmToX = x => (x - minX) * scale + (size - (maxX - minX) * scale) / 2
  mmToZ = z => (z - minZ) * scale + (size - (maxZ - minZ) * scale) / 2

  minimapCache = document.createElement('canvas')
  minimapCache.width = size
  minimapCache.height = size
  const ctx = minimapCache.getContext('2d')
  ctx.fillStyle = 'rgba(0,0,0,0.5)'
  ctx.fillRect(0, 0, size, size)
  ctx.strokeStyle = 'rgba(255,255,255,0.6)'
  ctx.lineWidth = 5
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.beginPath()
  for (let i = 0; i <= SAMPLE_COUNT; i++) {
    const s = trackSamples[i % SAMPLE_COUNT]
    const mx = mmToX(s.point.x)
    const mz = mmToZ(s.point.z)
    if (i === 0) ctx.moveTo(mx, mz)
    else ctx.lineTo(mx, mz)
  }
  ctx.closePath()
  ctx.stroke()
  // Start marker
  ctx.fillStyle = '#ffffff'
  ctx.beginPath()
  ctx.arc(mmToX(trackSamples[0].point.x), mmToZ(trackSamples[0].point.z), 3, 0, Math.PI * 2)
  ctx.fill()
}

function drawMinimap() {
  const canvas = minimapRef.value
  if (!canvas || !minimapCache) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.drawImage(minimapCache, 0, 0)

  // Kart position
  const kx = mmToX(kart.x)
  const kz = mmToZ(kart.z)
  ctx.fillStyle = '#e63946'
  ctx.beginPath()
  ctx.arc(kx, kz, 4, 0, Math.PI * 2)
  ctx.fill()
}

// ── game loop ───────────────────────────────────────────
function animate() {
  animationId = requestAnimationFrame(animate)
  const dt = Math.min(clock.getDelta(), 0.05)

  if (gameState.value === 'racing') {
    elapsedTime.value += dt
    updatePhysics(dt)
  }

  if (gameState.value === 'racing' || gameState.value === 'countdown' || gameState.value === 'paused') {
    updateKartTransform()
    updateCamera()
    // Shadow camera follows the kart so shadows stay crisp on the huge track
    if (sunLight) {
      sunLight.position.set(kart.x + 50, 80, kart.z + 30)
      sunLight.target.position.set(kart.x, 0, kart.z)
    }
    drawMinimap()
  }

  renderer.render(scene, camera)
}

// ── resize ──────────────────────────────────────────────
function onResize() {
  if (!renderer || !viewportRef.value) return
  const w = viewportRef.value.clientWidth
  const h = viewportRef.value.clientHeight
  renderer.setSize(w, h)
  camera.aspect = w / h
  camera.updateProjectionMatrix()
}

// ── lifecycle ───────────────────────────────────────────
onMounted(() => {
  bestTime.value = loadBestTime()
  initScene()
  computeMinimapBounds()
  window.addEventListener('keydown', onKeyDown)
  window.addEventListener('keyup', onKeyUp)
  window.addEventListener('resize', onResize)
  animate()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('keydown', onKeyDown)
  window.removeEventListener('keyup', onKeyUp)
  window.removeEventListener('resize', onResize)
  if (renderer) {
    renderer.dispose()
    if (renderer.domElement && renderer.domElement.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement)
    }
  }
  isRendererInitialized = false
})
</script>

<style scoped lang="scss">
.kart-game-page {
  position: relative;
  width: 100%;
  height: calc(100vh - 113px);
  min-height: 420px;
  overflow: hidden;
}

.game-viewport {
  position: absolute;
  inset: 0;

  canvas {
    display: block;
  }
}

// ── HUD ────────────────────────────────
.hud {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 5;
}

.hud-top {
  position: absolute;
  top: 14px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.hud-chip {
  padding: 6px 14px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.hud-timer {
  font-variant-numeric: tabular-nums;
  min-width: 110px;
  text-align: center;
}

.hud-best {
  opacity: 0.8;
  font-size: 13px;
}

.hud-speed {
  position: absolute;
  right: 24px;
  bottom: 24px;
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: #fff;
}

.speed-value {
  font-size: 42px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.speed-unit {
  font-size: 14px;
  opacity: 0.7;
}

.boost-badge {
  position: absolute;
  right: 24px;
  bottom: 80px;
  padding: 4px 14px;
  border-radius: 4px;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 1px;
  animation: boost-pulse 0.5s ease-in-out infinite alternate;
}

@keyframes boost-pulse {
  from { transform: scale(1); opacity: 0.9; }
  to { transform: scale(1.08); opacity: 1; }
}

.minimap {
  position: absolute;
  right: 20px;
  top: 60px;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.hud-hint {
  position: absolute;
  left: 20px;
  bottom: 20px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

// ── Overlays ───────────────────────────
.overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);

  &--transparent {
    background: transparent;
    backdrop-filter: none;
    pointer-events: none;
  }
}

.overlay-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 40px 60px;
  background: rgba(0, 0, 0, 0.85);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;

  &--compact {
    padding: 32px 48px;
  }
}

.overlay-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
  text-align: center;

  &--sm {
    font-size: 24px;
  }
}

.overlay-sub {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.overlay-actions {
  display: flex;
  gap: 12px;
}

.countdown-number {
  font-size: 80px;
  font-weight: 900;
  color: #fff;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  animation: countdown-pop 0.8s ease-out forwards;
}

@keyframes countdown-pop {
  0% { transform: scale(2); opacity: 0; }
  30% { transform: scale(1); opacity: 1; }
  80% { opacity: 1; }
  100% { transform: scale(0.8); opacity: 0; }
}

.result-time {
  font-size: 36px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.result-best {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.record-badge {
  padding: 4px 16px;
  border-radius: 4px;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

// ── Buttons ────────────────────────────
.btn-primary {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  background: #e63946;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;

  &:hover {
    background: #c1121f;
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

.btn-secondary {
  padding: 12px 28px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;

  &:hover {
    border-color: rgba(255, 255, 255, 0.4);
    color: #fff;
  }
}

// ── Controls guide ─────────────────────
.controls-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);

  span {
    margin-left: 8px;
  }
}

kbd {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 26px;
  padding: 0 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  font-family: inherit;
}

// ── Back button ────────────────────────
.back-btn {
  position: absolute;
  left: 16px;
  top: 14px;
  z-index: 8;
  padding: 6px 14px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.5);
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;

  &:hover {
    color: #fff;
    border-color: rgba(255, 255, 255, 0.3);
  }
}
</style>
