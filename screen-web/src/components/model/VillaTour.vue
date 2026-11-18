<template>
  <div class="villa-tour">
    <canvas ref="canvasRef" class="villa-canvas" aria-label="新中式三层别墅三维模型"></canvas>

    <header class="villa-top">
      <button type="button" class="tour-button" @click="router.push('/platform/models')">返回</button>
      <div class="title-block">
        <strong>{{ activeView.label }}</strong>
        <span>新中式三层别墅 · {{ activeView.info }}</span>
      </div>
      <button
        type="button"
        class="tour-button"
        :class="{ 'is-active': activeView.id === 'overview' }"
        @click="showView('overview')"
      >总览</button>
    </header>

    <nav class="villa-nav" aria-label="别墅视角切换">
      <div v-for="group in VILLA_VIEW_GROUPS" :key="group" class="nav-group">
        <span>{{ group }}</span>
        <button
          v-for="view in viewsByGroup(group)"
          :key="view.id"
          type="button"
          class="nav-item"
          :class="{ 'is-active': view.id === activeViewId }"
          @click="showView(view.id)"
        >{{ view.label }}</button>
      </div>
    </nav>

    <aside class="villa-panel">
      <h2>{{ activeView.label }}</h2>
      <p>{{ activeView.info }}</p>
      <dl>
        <div v-for="spec in VILLA_SPECS" :key="spec.label">
          <dt>{{ spec.label }}</dt>
          <dd>{{ spec.value }}</dd>
        </div>
      </dl>
    </aside>
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import * as THREE from 'three'
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js'
import {box, createRoomSets, cylinder} from '@/components/model/villa-rooms'
import {VILLA_SPECS, VILLA_VIEW_GROUPS, VILLA_VIEWS} from '@/const/villa'

const props = defineProps({
  initialView: {type: String, default: 'overview'}
})

const canvasRef = ref(null)
const activeViewId = ref(props.initialView)
const activeView = computed(() => VILLA_VIEWS.find(view => view.id === activeViewId.value) || VILLA_VIEWS[0])
const router = useRouter()

const viewsByGroup = group => VILLA_VIEWS.filter(view => view.group === group)

let renderer
let scene
let camera
let controls
let resizeObserver
let animationId = 0
let clock
let cameraAnimation = 0
let facadeGroup
let garageGroup
let landscapeGroup
let roomSets = new Map()
let waterMaterial
const disposables = {geometries: [], materials: [], textures: []}

function trackDisposable(value) {
  if (value?.isTexture) disposables.textures.push(value)
  else if (value?.isMaterial) disposables.materials.push(value)
  else if (value?.isBufferGeometry) disposables.geometries.push(value)
  return value
}

function canvasTexture(painter, size = 512, repeatX = 1, repeatY = 1) {
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  painter(canvas.getContext('2d'), size)
  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  texture.wrapS = THREE.RepeatWrapping
  texture.wrapT = THREE.RepeatWrapping
  texture.repeat.set(repeatX, repeatY)
  texture.anisotropy = Math.min(8, renderer.capabilities.getMaxAnisotropy())
  return trackDisposable(texture)
}

function createTextures() {
  const grain = (context, size, alpha, density, light) => {
    for (let index = 0; index < density; index += 1) {
      const x = Math.random() * size
      const y = Math.random() * size
      context.fillStyle = index % 2 ? `rgba(255,255,255,${alpha})` : `rgba(0,0,0,${alpha * light})`
      context.fillRect(x, y, Math.random() * 2.4, Math.random() * 2.4)
    }
  }

  const plasterTexture = canvasTexture((context, size) => {
    context.fillStyle = '#f4f1ea'
    context.fillRect(0, 0, size, size)
    grain(context, size, 0.05, 5200, 1.8)
  }, 512, 4, 3)

  const woodTexture = canvasTexture((context, size) => {
    context.fillStyle = '#a87650'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 90; index += 1) {
      const y = index * (size / 90) + Math.random() * 3
      context.strokeStyle = index % 4 ? 'rgba(70,38,18,.22)' : 'rgba(235,200,165,.13)'
      context.lineWidth = Math.random() * 2 + 0.4
      context.beginPath()
      context.moveTo(0, y)
      context.bezierCurveTo(size * .3, y + Math.random() * 8, size * .7, y - Math.random() * 8, size, y)
      context.stroke()
    }
    grain(context, size, 0.03, 2200, 2)
  }, 512, 3, 3)

  const darkWoodTexture = canvasTexture((context, size) => {
    context.fillStyle = '#513a2c'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 55; index += 1) {
      context.strokeStyle = index % 3 ? 'rgba(255,215,170,.08)' : 'rgba(10,5,0,.28)'
      context.lineWidth = Math.random() * 2.5 + 0.5
      context.beginPath()
      context.moveTo(0, index * (size / 55))
      context.lineTo(size, index * (size / 55) + Math.random() * 5)
      context.stroke()
    }
  }, 512, 3, 2)

  const roofTexture = canvasTexture((context, size) => {
    context.fillStyle = '#35383e'
    context.fillRect(0, 0, size, size)
    for (let row = 0; row < 12; row += 1) {
      const y = row * (size / 12)
      context.strokeStyle = 'rgba(255,255,255,.06)'
      context.lineWidth = 2
      context.beginPath()
      context.moveTo(0, y)
      context.lineTo(size, y)
      context.stroke()
      for (let column = 0; column < 16; column += 1) {
        const x = column * (size / 16) + (row % 2) * (size / 32)
        context.beginPath()
        context.arc(x, y, size / 38, Math.PI, 0)
        context.strokeStyle = 'rgba(0,0,0,.30)'
        context.stroke()
        context.fillStyle = 'rgba(255,255,255,.035)'
        context.fill()
      }
    }
  }, 512, 7, 4)

  const stoneTexture = canvasTexture((context, size) => {
    context.fillStyle = '#9b958c'
    context.fillRect(0, 0, size, size)
    for (let row = 0; row < 5; row += 1) {
      for (let column = 0; column < 5; column += 1) {
        const offset = (row % 2) * 45
        const x = (column * 105 + offset) % size
        const y = row * 105
        context.fillStyle = ['#8d8880', '#9a948b', '#a5a097', '#847f77'][column % 4]
        context.fillRect(x + 4, y + 4, 96, 96)
        grain(context, size, 0.06, 700, 2)
      }
    }
  }, 512, 5, 5)

  const grassTexture = canvasTexture((context, size) => {
    context.fillStyle = '#55714e'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 9000; index += 1) {
      const x = Math.random() * size
      const y = Math.random() * size
      context.strokeStyle = ['rgba(84,112,72,.7)', 'rgba(107,137,90,.55)', 'rgba(57,79,54,.6)'][index % 3]
      context.lineWidth = 1.5
      context.beginPath()
      context.moveTo(x, y)
      context.lineTo(x + Math.random() * 3 - 1.5, y - Math.random() * 7)
      context.stroke()
    }
  }, 512, 12, 12)

  const marbleTexture = canvasTexture((context, size) => {
    context.fillStyle = '#ded8cf'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 28; index += 1) {
      context.strokeStyle = index % 4 ? 'rgba(110,100,90,.13)' : 'rgba(70,65,60,.2)'
      context.lineWidth = Math.random() * 2.6 + 0.3
      context.beginPath()
      const startX = Math.random() * size
      context.moveTo(startX, 0)
      context.bezierCurveTo(startX + Math.random() * 160, size * .4, startX - Math.random() * 180, size * .7, Math.random() * size, size)
      context.stroke()
    }
  }, 512, 3, 3)

  const waterNormal = canvasTexture((context, size) => {
    context.fillStyle = '#808080'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 180; index += 1) {
      const x = Math.random() * size
      const y = Math.random() * size
      const radius = Math.random() * 26 + 7
      const gradient = context.createRadialGradient(x, y, 0, x, y, radius)
      gradient.addColorStop(0, 'rgba(220,220,255,.2)')
      gradient.addColorStop(1, 'rgba(90,90,140,0)')
      context.fillStyle = gradient
      context.fillRect(x - radius, y - radius, radius * 2, radius * 2)
    }
  }, 512, 3, 3)
  waterNormal.colorSpace = THREE.NoColorSpace

  const skyTexture = canvasTexture((context, size) => {
    const gradient = context.createLinearGradient(0, 0, 0, size)
    gradient.addColorStop(0, '#3f76c2')
    gradient.addColorStop(.48, '#8ec3ea')
    gradient.addColorStop(.72, '#d9eaf4')
    gradient.addColorStop(1, '#eae4d8')
    context.fillStyle = gradient
    context.fillRect(0, 0, size, size)
    context.fillStyle = 'rgba(255,255,255,.21)'
    for (let index = 0; index < 12; index += 1) {
      const x = Math.random() * size
      const y = size * (Math.random() * .43 + .08)
      context.beginPath()
      context.ellipse(x, y, Math.random() * 90 + 45, Math.random() * 20 + 8, 0, 0, Math.PI * 2)
      context.fill()
    }
  }, 1024, 1, 1)
  skyTexture.mapping = THREE.EquirectangularReflectionMapping

  const windowTexture = canvasTexture((context, size) => {
    const gradient = context.createLinearGradient(0, 0, 0, size)
    gradient.addColorStop(0, '#a8d4f2')
    gradient.addColorStop(.62, '#dfece7')
    gradient.addColorStop(1, '#7c8f6d')
    context.fillStyle = gradient
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 32; index += 1) {
      context.fillStyle = index % 3 ? 'rgba(55,80,50,.24)' : 'rgba(80,105,66,.34)'
      const x = Math.random() * size
      const y = size * (Math.random() * .4 + .5)
      context.beginPath()
      context.ellipse(x, y, Math.random() * 55 + 25, Math.random() * 90 + 40, Math.random() * .6, 0, Math.PI * 2)
      context.fill()
    }
  }, 512, 1, 1)

  return {
    plaster: plasterTexture,
    wood: woodTexture,
    darkWood: darkWoodTexture,
    roof: roofTexture,
    stone: stoneTexture,
    grass: grassTexture,
    marble: marbleTexture,
    waterNormal,
    sky: skyTexture,
    window: windowTexture
  }
}

function createMaterials(textures) {
  const create = (options) => trackDisposable(new THREE.MeshStandardMaterial(options))
  return {
    plaster: create({map: textures.plaster, roughness: .82, metalness: 0}),
    white: create({color: '#f6f4ee', roughness: .68, metalness: 0}),
    wood: create({map: textures.wood, roughness: .38, metalness: .02}),
    darkWood: create({map: textures.darkWood, roughness: .42, metalness: .03}),
    roof: create({map: textures.roof, roughness: .46, metalness: .05}),
    stone: create({map: textures.stone, roughness: .68, metalness: .02}),
    grass: create({map: textures.grass, roughness: .92, metalness: 0}),
    marble: create({map: textures.marble, roughness: .18, metalness: .04}),
    darkMetal: create({color: '#2d3238', roughness: .35, metalness: .68}),
    bronze: create({color: '#7a5c38', roughness: .3, metalness: .72}),
    glass: trackDisposable(new THREE.MeshPhysicalMaterial({
      color: '#a8cbd9', roughness: .06, metalness: .08, transparent: true,
      opacity: .24, envMapIntensity: 1.5, side: THREE.DoubleSide
    })),
    water: trackDisposable(new THREE.MeshStandardMaterial({
      color: '#2f8497', roughness: .04, metalness: .22, transparent: true,
      opacity: .8, normalMap: textures.waterNormal, normalScale: new THREE.Vector2(.52, .52), envMapIntensity: 1.6
    })),
    fabric: create({color: '#d8cfc0', roughness: .82, metalness: 0}),
    linen: create({color: '#e5ded3', roughness: .78, metalness: 0}),
    charcoal: create({color: '#383b40', roughness: .48, metalness: .08}),
    jade: create({color: '#3e6455', roughness: .32, metalness: .04}),
    sky: trackDisposable(new THREE.MeshBasicMaterial({map: textures.sky, side: THREE.BackSide}))
  }
}



function createGableRoof(parent, centerX, baseY, centerZ, width, depth, rise = 1.25) {
  const shape = new THREE.Shape()
  shape.moveTo(-width / 2, 0)
  shape.lineTo(width / 2, 0)
  shape.lineTo(0, rise)
  shape.closePath()
  const geometry = trackDisposable(new THREE.ExtrudeGeometry(shape, {depth, bevelEnabled: false}))
  const roof = new THREE.Mesh(geometry, materials.roof)
  roof.position.set(centerX, baseY, centerZ - depth / 2)
  roof.castShadow = true
  roof.receiveShadow = true
  parent.add(roof)
  box(parent, centerX, baseY - .08, centerZ - depth / 2 - .08, width + .3, .16, .2, materials.darkWood)
  box(parent, centerX, baseY - .08, centerZ + depth / 2 + .08, width + .3, .16, .2, materials.darkWood)
  return roof
}

function createWindowStrip(parent, centerX, centerY, centerZ, width, height, orientation = 'z', panels = 4) {
  const frameDepth = .12
  if (orientation === 'z') {
    box(parent, centerX, centerY, centerZ, width, height, frameDepth, materials.darkMetal)
    box(parent, centerX, centerY, centerZ + .02, width - .16, height - .16, .04, materials.glass, false)
  } else {
    box(parent, centerX, centerY, centerZ, frameDepth, height, width, materials.darkMetal)
    box(parent, centerX + .02, centerY, centerZ, .04, height - .16, width - .16, materials.glass, false)
  }
  for (let index = 1; index < panels; index += 1) {
    const offset = -width / 2 + (width / panels) * index
    if (orientation === 'z') {
      box(parent, centerX + offset, centerY, centerZ + .02, .05, height - .06, .08, materials.darkMetal)
    } else {
      box(parent, centerX + .02, centerY, centerZ + offset, .08, height - .06, .05, materials.darkMetal)
    }
  }
}

function createLattice(parent, centerX, centerY, centerZ, width, height, orientation = 'z', count = 14) {
  for (let index = 0; index < count; index += 1) {
    const offset = -width / 2 + (width / (count - 1)) * index
    if (orientation === 'z') {
      box(parent, centerX + offset, centerY, centerZ, .06, height, .08, materials.darkWood)
    } else {
      box(parent, centerX, centerY, centerZ + offset, .08, height, .06, materials.darkWood)
    }
  }
}

function createFacade(materialsRef) {
  facadeGroup = new THREE.Group()
  box(facadeGroup, -4, .16, -1, 10.6, .32, 11.6, materialsRef.stone)
  box(facadeGroup, -4, 2, -1, 10, 3.3, 11, materialsRef.plaster)
  box(facadeGroup, -4, 5.3, -1.25, 10, 3.3, 10, materialsRef.plaster)
  box(facadeGroup, -3, 8.55, -.75, 8, 3.2, 7, materialsRef.plaster)

  ;[3.62, 6.92].forEach((y, index) => {
    const depth = index ? 10 : 11
    const zCenter = index ? -1.25 : -1
    box(facadeGroup, -4, y, zCenter, index ? 10.2 : 10.2, .22, depth + .35, materials.darkWood)
  })

  createWindowStrip(facadeGroup, -6.4, 2.15, 4.52, 6.6, 2.1, 'z', 5)
  createWindowStrip(facadeGroup, -2.6, 2.15, 4.52, 4.0, 2.1, 'z', 3)
  createWindowStrip(facadeGroup, 1.02, 2.15, -3, 9.2, 2.0, 'x', 6)
  createWindowStrip(facadeGroup, -8.52, 2.15, -2, 8.0, 1.8, 'x', 5)
  createWindowStrip(facadeGroup, -6.4, 5.45, 4.02, 6.8, 2.1, 'z', 5)
  createWindowStrip(facadeGroup, -2.6, 5.45, 4.02, 4.0, 2.1, 'z', 3)
  createWindowStrip(facadeGroup, 1.02, 5.45, -2.5, 8.4, 2.0, 'x', 5)
  createWindowStrip(facadeGroup, -6.4, 8.7, 3.52, 5.8, 2.0, 'z', 4)
  createWindowStrip(facadeGroup, 1.02, 8.7, -.75, 6.4, 2.0, 'x', 4)

  createLattice(facadeGroup, -8.7, 4.7, 3.4, 7, 2.0, 'z', 22)
  createLattice(facadeGroup, -7.5, 7.9, 2.0, 5.5, 1.8, 'z', 18)
  ;[-7.5, -1.2].forEach(x => cylinder(facadeGroup, x, 5.3, 4.1, .13, .15, 3.3, materialsRef.darkWood, 16))
  ;[-7.5, -4.6, -1.6].forEach(x => cylinder(facadeGroup, x, 2, 4.25, .15, .17, 3.3, materialsRef.darkWood, 16))

  box(facadeGroup, -4, 3.78, 4.9, 10.5, .08, 1.8, materialsRef.darkWood)
  box(facadeGroup, -3, 7.08, 4.4, 8.4, .08, 1.6, materialsRef.darkWood)
  box(facadeGroup, -7.8, 6.9, 4.0, .1, .85, 1.4, materialsRef.glass, false)
  box(facadeGroup, -.2, 10.05, 2.5, .1, .9, 1.6, materialsRef.glass, false)
  createGableRoof(facadeGroup, -3, 10.15, -.75, 9.0, 8.0, 1.45)
  createGableRoof(facadeGroup, -4, 3.78, -6.9, 10.0, 1.7, .65)
  scene.add(facadeGroup)
}

function createGarage(materialsRef) {
  garageGroup = new THREE.Group()
  box(garageGroup, 4, .18, -1, 6.4, .36, 6.8, materialsRef.stone)
  box(garageGroup, 4, 1.7, -1, 6, 3.0, 6, materialsRef.plaster)
  box(garageGroup, 4, 3.24, -1, 6.7, .24, 6.7, materialsRef.darkWood)
  createGableRoof(garageGroup, 4, 3.38, -1, 7, 7, .85)
  box(garageGroup, 7.05, 1.6, -1, .16, 2.3, 4.5, materialsRef.darkWood)
  box(garageGroup, 7.02, 1.6, -1, .05, 2.1, 4.1, materialsRef.glass, false)
  for (let index = 0; index < 18; index += 1) {
    box(garageGroup, 7.08, 1.6, -3.1 + index * .24, .1, 2.2, .05, materialsRef.darkWood)
  }
  box(garageGroup, 10.2, .08, -1, 7, .16, 5.4, materialsRef.marble)
  scene.add(garageGroup)
}

function createTree(parent, x, z, scale = 1) {
  const group = new THREE.Group()
  group.position.set(x, 0, z)
  group.scale.setScalar(scale)
  cylinder(group, 0, 1, 0, .1, .2, 2, materials.darkWood, 12)
  const foliage = new THREE.SphereGeometry(1.3, 24, 18)
  const leafMaterial = materials.jade
  ;[
    [0, 2.4, 0, 1],
    [.8, 2.1, .35, .72],
    [-.7, 2.2, -.3, .68],
    [.2, 3.0, -.25, .58]
  ].forEach(([x, y, z, scalar]) => {
    const geometry = trackDisposable(foliage.clone())
    const mesh = new THREE.Mesh(geometry, leafMaterial)
    mesh.position.set(x, y, z)
    mesh.scale.setScalar(scalar)
    mesh.castShadow = true
    group.add(mesh)
  })
  parent.add(group)
}

function createBamboo(parent, x, z) {
  const group = new THREE.Group()
  group.position.set(x, 0, z)
  for (let index = 0; index < 7; index += 1) {
    const offset = (index - 3) * .16
    cylinder(group, offset, 1.4, offset * .4, .05, .06, 2.9, materials.jade, 8)
  }
  parent.add(group)
}

function createLandscape(materialsRef) {
  landscapeGroup = new THREE.Group()
  const ground = new THREE.Mesh(trackDisposable(new THREE.PlaneGeometry(52, 56)), materialsRef.grass)
  ground.rotation.x = -Math.PI / 2
  ground.receiveShadow = true
  landscapeGroup.add(ground)

  box(landscapeGroup, -4, .05, 7, 11.5, .12, 8.5, materialsRef.marble)
  for (let index = 0; index < 6; index += 1) {
    box(landscapeGroup, -4, .13, 5 + index * 1.2, 2.2, .06, .92, materialsRef.stone)
  }
  box(landscapeGroup, -8.6, 1.25, 7.2, .3, 2.2, 4.4, materialsRef.plaster)
  createLattice(landscapeGroup, -8.6, 2.15, 7.2, 4.2, 1.4, 'x', 16)
  box(landscapeGroup, -.4, 1.1, 7.6, .2, 1.9, .2, materialsRef.darkWood)
  cylinder(landscapeGroup, -.4, 2.1, 7.6, .32, .36, .16, materialsRef.darkWood, 24)

  box(landscapeGroup, 1, .05, 11, 13.5, .12, 6.5, materialsRef.wood)
  box(landscapeGroup, 1, .1, 11, 10.8, .18, 4.6, materialsRef.stone)
  box(landscapeGroup, 1, .14, 11, 10.1, .18, 3.9, materialsRef.jade)
  const water = box(landscapeGroup, 1, .3, 11, 9.8, .06, 3.6, materialsRef.water, false)
  waterMaterial = water.material
  waterMaterial.needsUpdate = true
  for (let index = 0; index < 48; index += 1) {
    const x = -4 + index * .21
    box(landscapeGroup, x, .31, 9.15, .04, .02, .12, materialsRef.glass, false)
    box(landscapeGroup, x, .31, 12.85, .04, .02, .12, materialsRef.glass, false)
  }
  ;[-3.8, -1.8, 3.8, 5.8].forEach(x => cylinder(landscapeGroup, x, .55, 13.4, .12, .14, .9, materialsRef.darkWood, 16))

  createTree(landscapeGroup, -13, 2, 1.15)
  createTree(landscapeGroup, -13.5, -5, 1.45)
  createTree(landscapeGroup, 7, -9, 1.25)
  createTree(landscapeGroup, 10, 4, .9)
  createBamboo(landscapeGroup, -11, 6)
  createBamboo(landscapeGroup, -11, -2)
  createBamboo(landscapeGroup, 8, 2)
  for (let index = 0; index < 8; index += 1) {
    cylinder(landscapeGroup, -9 + index * 2.4, .25, -11, .5, .55, .5, materialsRef.jade, 14)
    cylinder(landscapeGroup, -8 + index * 2.1, .2, 16, .7, .75, .35, materialsRef.jade, 16)
  }
  scene.add(landscapeGroup)
}














let materials
let textures

function setViewVisibility(view) {
  const interior = view.type === 'interior'
  facadeGroup.visible = !interior
  garageGroup.visible = !interior
  landscapeGroup.visible = !interior
  roomSets.forEach((group, roomId) => {
    group.visible = interior && roomId === view.roomId
  })
}

function applyControlLimits(view) {
  if (view.type === 'interior') {
    controls.minDistance = 1.6
    controls.maxDistance = 10
    controls.maxPolarAngle = Math.PI / 1.85
    controls.minPolarAngle = .12
  } else {
    controls.minDistance = 8
    controls.maxDistance = 44
    controls.maxPolarAngle = Math.PI / 2.08
    controls.minPolarAngle = .08
  }
}

function showView(viewId) {
  const view = VILLA_VIEWS.find(item => item.id === viewId)
  if (!view || view.id === activeViewId.value) return
  if (cameraAnimation) cancelAnimationFrame(cameraAnimation)
  setViewVisibility(view)
  activeViewId.value = view.id
  const startPosition = camera.position.clone()
  const startTarget = controls.target.clone()
  const endPosition = new THREE.Vector3(...view.camera)
  const endTarget = new THREE.Vector3(...view.target)
  const distance = startPosition.distanceTo(endPosition)
  const startTime = performance.now()
  const duration = Math.min(1500, Math.max(720, distance * 42))
  controls.enabled = false

  const tick = now => {
    const linear = Math.min(1, (now - startTime) / duration)
    const eased = linear < .5 ? 2 * linear * linear : 1 - Math.pow(-2 * linear + 2, 2) / 2
    camera.position.lerpVectors(startPosition, endPosition, eased)
    controls.target.lerpVectors(startTarget, endTarget, eased)
    camera.lookAt(controls.target)
    if (linear < 1) {
      cameraAnimation = requestAnimationFrame(tick)
      return
    }
    cameraAnimation = 0
    controls.target.copy(endTarget)
    applyControlLimits(view)
    controls.enabled = true
    controls.update()
  }
  cameraAnimation = requestAnimationFrame(tick)
}

function resize() {
  if (!canvasRef.value || !renderer) return
  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}

function disposeScene() {
  if (cameraAnimation) cancelAnimationFrame(cameraAnimation)
  if (animationId) cancelAnimationFrame(animationId)
  resizeObserver?.disconnect()
  controls?.dispose()
  scene?.traverse(child => {
    child.geometry?.dispose()
    const materialsToDispose = Array.isArray(child.material) ? child.material : [child.material]
    materialsToDispose.forEach(material => material?.dispose())
  })
  disposables.textures.forEach(texture => texture.dispose())
  disposables.materials.forEach(material => material.dispose())
  disposables.geometries.forEach(geometry => geometry.dispose())
  renderer?.dispose()
  renderer?.forceContextLoss()
  roomSets = new Map()
}

onMounted(async () => {
  await nextTick()
  renderer = new THREE.WebGLRenderer({canvas: canvasRef.value, antialias: true})
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.06
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap

  scene = new THREE.Scene()
  textures = createTextures()
  materials = createMaterials(textures)
  scene.environment = textures.sky
  scene.background = textures.sky

  camera = new THREE.PerspectiveCamera(48, 1, .1, 240)
  const initial = VILLA_VIEWS.find(view => view.id === props.initialView) || VILLA_VIEWS[0]
  camera.position.set(...initial.camera)
  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(...initial.target)
  controls.enableDamping = true
  controls.dampingFactor = .06
  controls.rotateSpeed = .58
  controls.zoomSpeed = .75
  controls.panSpeed = .55
  applyControlLimits(initial)

  scene.add(new THREE.HemisphereLight('#dfeaff', '#7d8f74', 1.45))
  const sunlight = new THREE.DirectionalLight('#fff3df', 2.6)
  sunlight.position.set(16, 24, 18)
  sunlight.castShadow = true
  sunlight.shadow.mapSize.set(2048, 2048)
  sunlight.shadow.camera.near = 1
  sunlight.shadow.camera.far = 95
  sunlight.shadow.camera.left = -30
  sunlight.shadow.camera.right = 30
  sunlight.shadow.camera.top = 30
  sunlight.shadow.camera.bottom = -30
  sunlight.shadow.bias = -.0004
  scene.add(sunlight)
  const fillLight = new THREE.DirectionalLight('#cadcf5', .48)
  fillLight.position.set(-18, 12, -14)
  scene.add(fillLight)

  const skyDome = new THREE.Mesh(trackDisposable(new THREE.SphereGeometry(120, 48, 32)), materials.sky)
  scene.add(skyDome)
  createFacade(materials)
  createGarage(materials)
  createLandscape(materials)
  roomSets = createRoomSets(scene, materials, textures)
  setViewVisibility(initial)

  resize()
  resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(canvasRef.value)

  const render = () => {
    animationId = requestAnimationFrame(render)
    const elapsed = clock.getElapsedTime()
    if (waterMaterial?.normalMap) {
      waterMaterial.normalMap.offset.set(Math.sin(elapsed * .22) * .05, elapsed * .035)
      waterMaterial.needsUpdate = true
    }
    controls.update()
    renderer.render(scene, camera)
  }
  clock = new THREE.Clock()
  render()
})

onBeforeUnmount(disposeScene)
</script>

<style scoped lang="scss">
.villa-tour {
  position: relative;
  min-height: calc(100vh - 112px);
  overflow: hidden;
  background: #c8d8e5;
}

.villa-canvas {
  display: block;
  width: 100%;
  height: calc(100vh - 112px);
}

.villa-top {
  position: absolute;
  top: 18px;
  left: 20px;
  right: 20px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 12px;
}

.tour-button {
  flex: 0 0 auto;
  min-width: 64px;
  height: 38px;
  border: 1px solid rgba(20, 32, 40, .18);
  border-radius: 6px;
  background: rgba(250, 250, 247, .92);
  color: #3d4a52;
  font-size: 14px;
  cursor: pointer;

  &.is-active {
    border-color: #22343d;
    background: #22343d;
    color: #fff;
  }
}

.title-block {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, .26);
  border-radius: 6px;
  background: rgba(19, 28, 32, .52);
  backdrop-filter: blur(12px);
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

.villa-nav {
  position: absolute;
  bottom: 18px;
  left: 20px;
  right: 20px;
  z-index: 3;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px 16px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, .2);
  border-radius: 8px;
  background: rgba(17, 26, 30, .52);
  backdrop-filter: blur(14px);
}

.nav-group {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;

  span {
    flex: 0 0 34px;
    color: rgba(255, 255, 255, .55);
    font-size: 12px;
  }
}

.nav-item {
  flex: 1 1 auto;
  min-width: 44px;
  height: 32px;
  padding: 0 4px;
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: rgba(255, 255, 255, .8);
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

.villa-panel {
  position: absolute;
  top: 82px;
  right: 20px;
  z-index: 3;
  width: 248px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, .2);
  border-radius: 8px;
  background: rgba(17, 26, 30, .62);
  backdrop-filter: blur(16px);
  color: #fff;

  h2 {
    margin: 0;
    color: #f4e8cd;
    font-size: 18px;
    line-height: 24px;
  }

  p {
    min-height: 64px;
    margin: 10px 0 16px;
    color: rgba(255, 255, 255, .76);
    font-size: 13px;
    line-height: 21px;
  }
}

.villa-panel dl {
  display: grid;
  gap: 10px;
  margin: 0;
}

.villa-panel dl div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.villa-panel dt {
  color: rgba(255, 255, 255, .52);
  font-size: 12px;
}

.villa-panel dd {
  margin: 0;
  color: #fff;
  font-size: 12px;
  text-align: right;
}
</style>
