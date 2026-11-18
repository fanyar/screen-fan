import * as THREE from 'three'
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js'

let materials
let textures
let waterMaterial
const disposables = {
  geometries: [],
  materials: [],
  textures: []
}
const swayMeshes = []

function trackDisposable(value) {
  if (value?.isTexture) disposables.textures.push(value)
  else if (value?.isMaterial) disposables.materials.push(value)
  else if (value?.isBufferGeometry) disposables.geometries.push(value)
  return value
}

function canvasTexture(painter, width = 512, height = width, repeatX = 1, repeatY = 1) {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  painter(canvas.getContext('2d'), width, height)
  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  texture.wrapS = THREE.RepeatWrapping
  texture.wrapT = THREE.RepeatWrapping
  texture.repeat.set(repeatX, repeatY)
  return trackDisposable(texture)
}

function grain(context, size, alpha, density, light = 1.8) {
  for (let index = 0; index < density; index += 1) {
    const x = Math.random() * size
    const y = Math.random() * size
    context.fillStyle = index % 2
      ? `rgba(255,255,255,${alpha})`
      : `rgba(0,0,0,${alpha * light})`
    context.fillRect(x, y, Math.random() * 2.2 + .4, Math.random() * 2.2 + .4)
  }
}

function createTextures() {
  const plaster = canvasTexture((context, size) => {
    context.fillStyle = '#f3f0e6'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 80; index += 1) {
      const y = size * (index / 80 + Math.random() * .02)
      context.fillStyle = `rgba(125,118,100,${.018 + Math.random() * .03})`
      context.fillRect(0, y, size, Math.random() * 10 + 3)
    }
    grain(context, size, .035, 6500, 1.7)
  }, 1024, 1024, 3, 2)

  const roof = canvasTexture((context, size) => {
    context.fillStyle = '#3a4048'
    context.fillRect(0, 0, size, size)
    const rowHeight = size / 17
    for (let row = 0; row < 17; row += 1) {
      const y = row * rowHeight
      for (let column = 0; column < 24; column += 1) {
        const x = column * (size / 24) + (row % 2) * (size / 48)
        const tile = context.createLinearGradient(x, y, x, y + rowHeight)
        tile.addColorStop(0, '#4b535d')
        tile.addColorStop(.58, '#333a42')
        tile.addColorStop(1, '#21262d')
        context.fillStyle = tile
        context.beginPath()
        context.ellipse(x + size / 48, y + rowHeight * .58, size / 48, rowHeight * .52, 0, 0, Math.PI * 2)
        context.fill()
        context.strokeStyle = 'rgba(198,208,218,.06)'
        context.stroke()
      }
      context.fillStyle = `rgba(228,238,246,${.045 + Math.random() * .04})`
      context.fillRect(0, y, size, 1.5)
    }
    grain(context, size, .025, 3600, 2.3)
  }, 1024, 1024, 7, 5)

  const createWood = (base, streak, dark, count) => canvasTexture((context, size) => {
    context.fillStyle = base
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < count; index += 1) {
      const x = Math.random() * size
      context.strokeStyle = index % 4 ? streak : dark
      context.lineWidth = Math.random() * 2.5 + .3
      context.beginPath()
      context.moveTo(x, 0)
      context.bezierCurveTo(
        x + Math.random() * 18 - 9,
        size * .32,
        x + Math.random() * 24 - 12,
        size * .68,
        x + Math.random() * 12 - 6,
        size
      )
      context.stroke()
    }
    grain(context, size, .025, 2800, 2)
  }, 1024, 1024, 4, 2)

  const wood = createWood('#7c553b', 'rgba(55,32,18,.17)', 'rgba(227,188,147,.09)', 90)
  const darkWood = createWood('#4c352a', 'rgba(22,10,5,.22)', 'rgba(181,136,101,.08)', 68)
  const floorWood = createWood('#6e4d36', 'rgba(42,25,14,.16)', 'rgba(205,168,124,.07)', 120)

  const stone = canvasTexture((context, size) => {
    context.fillStyle = '#8d8980'
    context.fillRect(0, 0, size, size)
    for (let row = 0; row < 7; row += 1) {
      const height = size / 7
      const offset = (row % 2) * size / 14
      for (let column = -1; column < 8; column += 1) {
        const x = column * (size / 6.4) + offset
        const shade = ['#9b968d', '#878278', '#a49e93', '#7d786e', '#938e84'][Math.floor(Math.random() * 5)]
        context.fillStyle = shade
        context.fillRect(x + 3, row * height + 3, size / 6.4 - 7, height - 7)
        context.strokeStyle = 'rgba(45,42,36,.22)'
        context.strokeRect(x + 3, row * height + 3, size / 6.4 - 7, height - 7)
      }
    }
    grain(context, size, .035, 6200, 2.2)
  }, 1024, 1024, 7, 7)

  const moss = canvasTexture((context, size) => {
    context.fillStyle = '#58684c'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 260; index += 1) {
      const x = Math.random() * size
      const y = Math.random() * size
      const radius = Math.random() * 38 + 12
      const patch = context.createRadialGradient(x, y, 0, x, y, radius)
      patch.addColorStop(0, ['rgba(93,112,72,.45)', 'rgba(70,89,60,.42)', 'rgba(111,127,81,.32)'][index % 3])
      patch.addColorStop(1, 'rgba(70,89,60,0)')
      context.fillStyle = patch
      context.beginPath()
      context.arc(x, y, radius, 0, Math.PI * 2)
      context.fill()
    }
    context.lineWidth = 1.7
    for (let index = 0; index < 9200; index += 1) {
      const x = Math.random() * size
      const y = Math.random() * size
      context.strokeStyle = ['rgba(101,122,75,.52)', 'rgba(68,88,58,.55)', 'rgba(126,140,89,.34)', 'rgba(48,63,44,.44)'][index % 4]
      context.beginPath()
      context.moveTo(x, y)
      context.lineTo(x + Math.random() * 4 - 2, y - Math.random() * 9 - 3)
      context.stroke()
    }
  }, 1024, 1024, 9, 7)

  const rock = canvasTexture((context, size) => {
    context.fillStyle = '#787165'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 45; index += 1) {
      const y = Math.random() * size
      context.strokeStyle = index % 3 ? 'rgba(47,40,33,.16)' : 'rgba(190,183,167,.08)'
      context.lineWidth = Math.random() * 7 + 2
      context.beginPath()
      context.moveTo(0, y)
      context.bezierCurveTo(size * .35, y + Math.random() * 30 - 15, size * .68, y - Math.random() * 30 + 15, size, y)
      context.stroke()
    }
    grain(context, size, .045, 10000, 2.4)
  }, 1024, 1024, 3, 3)

  const waterNormals = canvasTexture((context, size) => {
    context.fillStyle = '#8080ff'
    context.fillRect(0, 0, size, size)
    for (let index = 0; index < 260; index += 1) {
      const x = Math.random() * size
      const y = Math.random() * size
      const radius = Math.random() * 28 + 8
      const ripple = context.createRadialGradient(x, y, 0, x, y, radius)
      ripple.addColorStop(0, 'rgba(200,205,255,.18)')
      ripple.addColorStop(.72, 'rgba(90,95,180,.09)')
      ripple.addColorStop(1, 'rgba(128,128,255,0)')
      context.fillStyle = ripple
      context.fillRect(x - radius, y - radius, radius * 2, radius * 2)
    }
  }, 1024, 1024)
  waterNormals.colorSpace = THREE.NoColorSpace
  waterNormals.repeat.set(.08, .08)

  const sky = canvasTexture((context, width, height) => {
    const gradient = context.createLinearGradient(0, 0, 0, height)
    gradient.addColorStop(0, '#346fb8')
    gradient.addColorStop(.34, '#7db0d9')
    gradient.addColorStop(.58, '#bcd8e5')
    gradient.addColorStop(.76, '#e4ece2')
    gradient.addColorStop(1, '#eae3cf')
    context.fillStyle = gradient
    context.fillRect(0, 0, width, height)
    for (let index = 0; index < 52; index += 1) {
      const x = Math.random() * width
      const y = height * (Math.random() * .44 + .06)
      const radiusX = Math.random() * 145 + 42
      const cloud = context.createRadialGradient(x, y, 0, x, y, radiusX)
      cloud.addColorStop(0, `rgba(255,255,255,${.16 + Math.random() * .17})`)
      cloud.addColorStop(1, 'rgba(255,255,255,0)')
      context.fillStyle = cloud
      context.beginPath()
      context.ellipse(x, y, radiusX, radiusX * .29, 0, 0, Math.PI * 2)
      context.fill()
    }
  }, 2048, 1024, 1, 1)
  sky.mapping = THREE.EquirectangularReflectionMapping

  const mountains = canvasTexture((context, width, height) => {
    const layers = [
      {y: height * .44, color: 'rgba(150,168,177,.32)', amp: 52},
      {y: height * .57, color: 'rgba(113,134,133,.46)', amp: 40},
      {y: height * .72, color: 'rgba(82,101,91,.62)', amp: 30}
    ]
    layers.forEach(layer => {
      context.fillStyle = layer.color
      context.beginPath()
      context.moveTo(0, height)
      context.lineTo(0, layer.y)
      for (let x = 0; x <= width; x += width / 90) {
        const wave = Math.sin(x / width * Math.PI * 8 + layer.amp) * layer.amp * .34
        const detail = Math.sin(x / width * Math.PI * 27 + layer.amp * 2) * layer.amp * .16
        context.lineTo(x, layer.y - wave - detail)
      }
      context.lineTo(width, height)
      context.closePath()
      context.fill()
    })
  }, 2048, 512, 1, 1)

  const leaf = canvasTexture((context, size) => {
    context.clearRect(0, 0, size, size)
    for (let index = 0; index < 720; index += 1) {
      const x = size / 2 + (Math.random() - .5) * size * .82
      const y = size / 2 + (Math.random() - .5) * size * .82
      const distance = Math.hypot(x - size / 2, y - size / 2) / (size / 2)
      if (distance > .94 || Math.random() > 1.1 - distance * .65) continue
      context.save()
      context.translate(x, y)
      context.rotate(Math.random() * Math.PI)
      context.fillStyle = ['rgba(65,89,50,.94)', 'rgba(84,111,59,.92)', 'rgba(105,128,65,.82)', 'rgba(46,68,43,.90)'][index % 4]
      context.beginPath()
      context.ellipse(0, 0, Math.random() * 8 + 7, Math.random() * 3.6 + 2.4, 0, 0, Math.PI * 2)
      context.fill()
      context.restore()
    }
  }, 512, 512, 1, 1)

  const lotus = canvasTexture((context, size) => {
    context.clearRect(0, 0, size, size)
    context.translate(size / 2, size / 2)
    context.fillStyle = 'rgba(48,91,52,.95)'
    context.beginPath()
    context.moveTo(size * .47, 0)
    context.arc(0, 0, size * .47, -.25, Math.PI * 2 + .25)
    context.closePath()
    context.fill()
    context.strokeStyle = 'rgba(173,201,120,.55)'
    context.lineWidth = 3
    for (let index = 0; index < 18; index += 1) {
      context.beginPath()
      context.moveTo(0, 0)
      context.lineTo(Math.cos(index / 18 * Math.PI * 2) * size * .45, Math.sin(index / 18 * Math.PI * 2) * size * .45)
      context.stroke()
    }
  }, 256, 256, 1, 1)

  return {plaster, roof, wood, darkWood, floorWood, stone, moss, rock, waterNormals, sky, mountains, leaf, lotus}
}

function createMaterials(textures) {
  const standard = options => trackDisposable(new THREE.MeshStandardMaterial(options))
  return {
    plaster: standard({map: textures.plaster, roughness: .83, metalness: 0}),
    roof: standard({map: textures.roof, roughness: .47, metalness: .05}),
    wood: standard({map: textures.wood, roughness: .39, metalness: .02}),
    darkWood: standard({map: textures.darkWood, roughness: .42, metalness: .03}),
    floorWood: standard({map: textures.floorWood, roughness: .31, metalness: .02}),
    stone: standard({map: textures.stone, roughness: .65, metalness: .02}),
    moss: standard({map: textures.moss, color: '#7d8c6f', roughness: .93, metalness: 0}),
    rock: standard({color: '#7c7367', roughness: .86, metalness: .01, flatShading: true}),
    pathStone: standard({map: textures.stone, color: '#b7b2a7', roughness: .78, metalness: .02}),
    white: standard({color: '#f4f2ea', roughness: .74, metalness: 0}),
    bronze: standard({color: '#7b5b38', roughness: .31, metalness: .69}),
    charcoal: standard({color: '#31353a', roughness: .46, metalness: .06}),
    bamboo: standard({color: '#6f834b', roughness: .48, metalness: .02}),
    pondBed: standard({color: '#16302d', roughness: .86, metalness: 0}),
    water: trackDisposable(new THREE.MeshPhysicalMaterial({
      color: '#1a463f',
      roughness: .07,
      metalness: .04,
      transparent: true,
      opacity: .94,
      normalMap: textures.waterNormals,
      normalScale: new THREE.Vector2(.42, .42),
      clearcoat: 1,
      clearcoatRoughness: .07,
      envMapIntensity: 1.65,
      side: THREE.DoubleSide
    })),
    foliage: standard({
      map: textures.leaf,
      transparent: true,
      alphaTest: .24,
      side: THREE.DoubleSide,
      roughness: .71,
      metalness: 0,
      envMapIntensity: .55
    }),
    lotus: standard({
      map: textures.lotus,
      transparent: true,
      alphaTest: .22,
      side: THREE.DoubleSide,
      roughness: .55,
      metalness: 0
    }),
    sky: trackDisposable(new THREE.MeshBasicMaterial({
      map: textures.sky,
      side: THREE.BackSide,
      fog: false,
      depthWrite: false
    })),
    mountains: trackDisposable(new THREE.MeshBasicMaterial({
      map: textures.mountains,
      transparent: true,
      fog: true,
      depthWrite: false
    }))
  }
}

function box(parent, x, y, z, width, height, depth, material, cast = true) {
  const mesh = new THREE.Mesh(trackDisposable(new THREE.BoxGeometry(width, height, depth)), material)
  mesh.position.set(x, y, z)
  mesh.castShadow = cast
  mesh.receiveShadow = true
  parent.add(mesh)
  return mesh
}

function cylinder(parent, x, y, z, topRadius, bottomRadius, height, material, segments = 20) {
  const mesh = new THREE.Mesh(trackDisposable(new THREE.CylinderGeometry(topRadius, bottomRadius, height, segments)), material)
  mesh.position.set(x, y, z)
  mesh.castShadow = true
  mesh.receiveShadow = true
  parent.add(mesh)
  return mesh
}

function createGableRoof(parent, centerX, baseY, centerZ, ridgeLength, eaveDepth, rise, alongX = true) {
  const shape = new THREE.Shape()
  shape.moveTo(-eaveDepth / 2, 0)
  shape.quadraticCurveTo(-eaveDepth / 6, rise * .68, 0, rise)
  shape.quadraticCurveTo(eaveDepth / 6, rise * .68, eaveDepth / 2, 0)
  const geometry = trackDisposable(new THREE.ExtrudeGeometry(shape, {
    depth: ridgeLength,
    bevelEnabled: true,
    bevelSize: .04,
    bevelThickness: .04,
    bevelSegments: 1
  }))
  const roof = new THREE.Mesh(geometry, materials.roof)
  roof.castShadow = true
  roof.receiveShadow = true
  if (alongX) {
    roof.rotation.y = Math.PI / 2
    roof.position.set(centerX - ridgeLength / 2, baseY, centerZ)
  } else {
    roof.position.set(centerX, baseY, centerZ - ridgeLength / 2)
  }
  parent.add(roof)
  const ridge = box(parent, centerX, baseY + rise + .04, centerZ,
    alongX ? ridgeLength + .16 : .24,
    .14,
    alongX ? .24 : ridgeLength + .16,
    materials.darkWood, false)
  ridge.receiveShadow = false
  return roof
}

function createRock(parent, x, y, z, scale = 1, seed = Math.random()) {
  const geometry = trackDisposable(new THREE.DodecahedronGeometry(1, 1))
  const position = geometry.attributes.position
  for (let index = 0; index < position.count; index += 1) {
    const multiplier = .78 + Math.sin(index * 5.3 + seed * 18) * .16 + Math.random() * .1
    position.setXYZ(
      index,
      position.getX(index) * multiplier,
      position.getY(index) * multiplier * (1 + Math.sin(seed * 7 + index * .2) * .18),
      position.getZ(index) * multiplier * .82
    )
  }
  geometry.computeVertexNormals()
  const mesh = new THREE.Mesh(geometry, materials.rock)
  mesh.position.set(x, y, z)
  mesh.rotation.set(seed * .6, seed * Math.PI, seed * .4)
  mesh.scale.set(scale * (1.1 + seed * .35), scale * (.75 + seed * .42), scale * (.82 + seed * .28))
  mesh.castShadow = true
  mesh.receiveShadow = true
  parent.add(mesh)
}

function createFoliagePlane(group, x, y, z, size, rotationY, tilt, material) {
  const geometry = trackDisposable(new THREE.PlaneGeometry(size, size * .92, 3, 3))
  const position = geometry.attributes.position
  for (let index = 0; index < position.count; index += 1) {
    const vertexY = position.getY(index)
    position.setZ(index, Math.sin(vertexY * 1.7) * size * .045)
  }
  geometry.computeVertexNormals()
  const mesh = new THREE.Mesh(geometry, material)
  mesh.position.set(x, y, z)
  mesh.rotation.set(tilt, rotationY, Math.random() * .18 - .09)
  mesh.castShadow = true
  mesh.receiveShadow = false
  group.add(mesh)
  swayMeshes.push(mesh)
  return mesh
}

function createTree(parent, x, z, scale = 1, type = 'broadleaf') {
  const group = new THREE.Group()
  group.position.set(x, 0, z)
  group.scale.setScalar(scale)
  const trunkHeight = type === 'willow' ? 3.2 : 4.2
  cylinder(group, 0, trunkHeight / 2, 0, .14, .28, trunkHeight, materials.darkWood, 14)
  if (type !== 'bamboo') {
    const branchCount = 5
    for (let index = 0; index < branchCount; index += 1) {
      const angle = index / branchCount * Math.PI * 2 + Math.random() * .4
      const radius = type === 'willow' ? .8 : 1.1
      const branch = cylinder(
        group,
        Math.cos(angle) * radius * .45,
        trunkHeight * .78,
        Math.sin(angle) * radius * .45,
        .03,
        .09,
        1.8,
        materials.darkWood,
        9
      )
      branch.rotation.set(Math.sin(angle) * .8, angle, Math.cos(angle) * .8)
    }
  }

  if (type === 'bamboo') {
    for (let stem = 0; stem < 9; stem += 1) {
      const ox = (Math.random() - .5) * 1.5
      const oz = (Math.random() - .5) * 1.2
      cylinder(group, ox, 2.6 + Math.random() * .6, oz, .035, .055, 5.4 + Math.random(), materials.bamboo, 8)
      for (let layer = 0; layer < 3; layer += 1) {
        createFoliagePlane(group, ox, 4 + layer * .9, oz, .95, Math.random() * Math.PI, .55, materials.foliage)
      }
    }
  } else if (type === 'willow') {
    for (let index = 0; index < 18; index += 1) {
      const angle = index / 18 * Math.PI * 2
      const radius = 1.5 + Math.random() * .7
      const x = Math.cos(angle) * radius
      const z = Math.sin(angle) * radius
      const plane = createFoliagePlane(group, x, 3.0 + Math.random() * .5, z, 2.2, angle, .12, materials.foliage)
      plane.rotation.x = 1.32
    }
  } else {
    const layers = 4
    for (let layer = 0; layer < layers; layer += 1) {
      const y = trunkHeight * .72 + layer * .58
      const radius = 2.2 - layer * .38
      const count = 6
      for (let index = 0; index < count; index += 1) {
        const angle = (index + layer * .48) / count * Math.PI * 2
        createFoliagePlane(
          group,
          Math.cos(angle) * radius,
          y,
          Math.sin(angle) * radius,
          2.3 - layer * .25,
          angle,
          .16 + layer * .1,
          materials.foliage
        )
      }
    }
  }
  parent.add(group)
}

function createLotus(parent, x, z, scale = 1) {
  const group = new THREE.Group()
  group.position.set(x, .52, z)
  group.scale.setScalar(scale)
  for (let index = 0; index < 3; index += 1) {
    const ox = (Math.random() - .5) * 1.5
    const oz = (Math.random() - .5) * 1.5
    const leafGeometry = trackDisposable(new THREE.CircleGeometry(.35 + Math.random() * .2, 24))
    const leaf = new THREE.Mesh(leafGeometry, materials.lotus)
    leaf.rotation.x = -Math.PI / 2
    leaf.rotation.z = Math.random() * Math.PI
    leaf.position.set(ox, Math.random() * .03, oz)
    leaf.castShadow = false
    leaf.receiveShadow = true
    group.add(leaf)
  }
  if (Math.random() > .58) {
    const flower = new THREE.Group()
    for (let petal = 0; petal < 7; petal += 1) {
      const angle = petal / 7 * Math.PI * 2
      const geometry = trackDisposable(new THREE.SphereGeometry(.08, 8, 6))
      const mesh = new THREE.Mesh(geometry, materials.white)
      mesh.position.set(Math.cos(angle) * .06, .16, Math.sin(angle) * .06)
      mesh.scale.set(.7, 2.2, .7)
      mesh.rotation.set(Math.sin(angle) * .5, angle, Math.cos(angle) * .5)
      flower.add(mesh)
    }
    flower.position.set((Math.random() - .5) * .8, .06, (Math.random() - .5) * .8)
    group.add(flower)
  }
  parent.add(group)
}

function createPondShape() {
  const points = [
    [-31, -18], [-13, -25], [10, -23], [28, -15],
    [37, 1], [30, 18], [10, 24], [-12, 22],
    [-28, 14], [-38, -2]
  ]
  const shape = new THREE.Shape()
  shape.moveTo(points[0][0], points[0][1])
  for (let index = 1; index <= points.length; index += 1) {
    const [x, y] = points[index % points.length]
    const previous = points[index - 1]
    const centerX = (previous[0] + x) / 2
    const centerY = (previous[1] + y) / 2
    shape.quadraticCurveTo(previous[0], previous[1], centerX, centerY)
  }
  shape.closePath()
  return shape
}

function createLandscape(scene) {
  const landscape = new THREE.Group()
  scene.add(landscape)

  const groundGeometry = trackDisposable(new THREE.PlaneGeometry(380, 250, 72, 50))
  const groundPosition = groundGeometry.attributes.position
  for (let index = 0; index < groundPosition.count; index += 1) {
    const x = groundPosition.getX(index)
    const y = groundPosition.getY(index)
    const edge = Math.max(Math.abs(x) / 180, Math.abs(y) / 115)
    groundPosition.setZ(index, edge > .58 ? (edge - .58) * 16 + Math.sin(x * .07 + y * .05) * .8 : Math.sin(x * .09 + y * .08) * .12)
  }
  groundGeometry.computeVertexNormals()
  const ground = new THREE.Mesh(groundGeometry, materials.moss)
  ground.rotation.x = -Math.PI / 2
  ground.receiveShadow = true
  landscape.add(ground)

  const bed = new THREE.Mesh(trackDisposable(new THREE.PlaneGeometry(86, 58)), materials.charcoal)
  bed.rotation.x = -Math.PI / 2
  bed.position.y = -.5
  bed.receiveShadow = false
  landscape.add(bed)

  const pondGeometry = trackDisposable(new THREE.ShapeGeometry(createPondShape(), 36))
  pondGeometry.rotateX(-Math.PI / 2)
  const pondBedGeometry = trackDisposable(new THREE.ShapeGeometry(createPondShape(), 36))
  pondBedGeometry.rotateX(-Math.PI / 2)
  const pondBed = new THREE.Mesh(pondBedGeometry, materials.pondBed)
  pondBed.position.y = .08
  pondBed.receiveShadow = false
  scene.add(pondBed)
  const water = new THREE.Mesh(pondGeometry, materials.water)
  water.position.y = .42
  water.receiveShadow = true
  waterMaterial = water.material
  scene.add(water)

  const shorePoints = [[-33, -15], [-15, -23], [8, -21], [26, -13], [34, 0], [28, 16], [9, 22], [-13, 20], [-27, 13], [-36, -1]]
  for (let index = 0; index < shorePoints.length; index += 1) {
    const [x, z] = shorePoints[index]
    createRock(landscape, x, .28, z, .75 + Math.random() * .35, index * .31)
    if (index % 2 === 0) createRock(landscape, x + 2.2, .18, z + 1.5, .38, index * .53)
  }

  for (let index = 0; index < 86; index += 1) {
    const angle = index / 86 * Math.PI * 2
    const radius = 49 + Math.sin(index * 1.7) * 4
    const x = Math.cos(angle) * radius
    const z = Math.sin(angle) * radius * .78
    const slab = box(landscape, x, .09, z, 2.1, .12, 1.5, materials.pathStone, false)
    slab.rotation.y = angle + Math.random() * .12
    if (index % 5 === 0) createRock(landscape, x + Math.cos(angle) * 3, .2, z + Math.sin(angle) * 3, .42, index * .19)
  }

  const treeSpots = [
    [-42, -20, 1.45, 'willow'], [-46, 2, 1.2, 'broadleaf'], [-40, 17, 1.35, 'willow'],
    [-26, -34, 1.5, 'broadleaf'], [-4, -36, 1.2, 'bamboo'], [16, -34, 1.35, 'broadleaf'],
    [33, -24, 1.4, 'willow'], [42, -6, 1.3, 'broadleaf'], [43, 9, 1.15, 'bamboo'],
    [38, 25, 1.4, 'broadleaf'], [22, 34, 1.25, 'willow'], [-4, 38, 1.45, 'broadleaf'],
    [-25, 32, 1.3, 'bamboo'], [-43, 26, 1.35, 'willow']
  ]
  treeSpots.forEach(([x, z, scale, type]) => createTree(landscape, x, z, scale, type))

  for (let index = 0; index < 70; index += 1) {
    const x = (Math.random() - .5) * 58
    const z = (Math.random() - .5) * 37
    if (Math.hypot(x + 5, z - .5) < 5 || Math.hypot(x - 8, z - 4) < 6) continue
    createLotus(landscape, x, z, 1.05 + Math.random() * .65)
  }

  createRockery(landscape)
  createDistantMountains(scene)
  return water
}

function createRockery(parent) {
  const rocks = new THREE.Group()
  parent.add(rocks)
  const clusters = [[-9, -9, 1.25], [17, -11, 1.05], [-20, 8, .72]]
  clusters.forEach(([x, z, scale], clusterIndex) => {
    for (let index = 0; index < 7; index += 1) {
      const angle = index / 9 * Math.PI * 2 + clusterIndex
      const radius = scale * (.52 + Math.random() * .82)
      createRock(
        rocks,
        x + Math.cos(angle) * radius,
        .22 + index * .06,
        z + Math.sin(angle) * radius * .72,
        scale * (.28 + Math.random() * .34),
        clusterIndex * 3 + index * .29
      )
    }
  })
  createTree(rocks, -11.5, -12.5, .72, 'broadleaf')
  createTree(rocks, 20.5, -13.5, .62, 'bamboo')
}

function createDistantMountains(scene) {
  const layerOne = new THREE.Mesh(trackDisposable(new THREE.PlaneGeometry(780, 180)), materials.mountains)
  layerOne.position.set(0, 38, -230)
  layerOne.renderOrder = -2
  scene.add(layerOne)
  const layerTwo = new THREE.Mesh(trackDisposable(new THREE.PlaneGeometry(620, 125)), materials.mountains)
  layerTwo.position.set(65, 27, -195)
  layerTwo.material = materials.mountains
  layerTwo.rotation.y = .18
  layerTwo.renderOrder = -1
  scene.add(layerTwo)
}

function createHall(parent, x, z, width = 15, depth = 9, rotationY = 0) {
  const group = new THREE.Group()
  group.position.set(x, 0, z)
  group.rotation.y = rotationY
  parent.add(group)
  box(group, 0, .30, 0, width + 2.6, .60, depth + 2.4, materials.stone)
  box(group, 0, .68, 0, width, .16, depth, materials.floorWood, false)
  const columnPositions = []
  for (let column = 0; column <= 4; column += 1) {
    columnPositions.push(-width / 2 + column * (width / 4))
  }
  columnPositions.forEach(offsetX => {
    ;[-depth / 2, depth / 2].forEach(offsetZ => {
      cylinder(group, offsetX, 2.5, offsetZ, .16, .19, 3.5, materials.darkWood, 16)
    })
  })
  box(group, 0, 2.0, -depth / 2 + .08, width - 1.2, 2.9, .14, materials.plaster)
  for (let panel = 0; panel < 5; panel += 1) {
    const offsetX = -width / 2 + 1.3 + panel * ((width - 2.6) / 4)
    box(group, offsetX, 2.0, -depth / 2 + .22, 1.9, 2.5, .04, materials.darkWood, false)
  }
  ;[-1, 1].forEach(side => {
    box(group, side * (width / 2 + .04), 1.1, 0, .12, .85, depth - 1, materials.plaster)
    for (let column = 0; column <= 3; column += 1) {
      const offsetZ = -depth / 2 + .8 + column * ((depth - 1.6) / 3)
      box(group, side * (width / 2 + .04), 2.1, offsetZ, .08, 1.9, 1.4, materials.darkWood, false)
    }
  })
  createGableRoof(group, 0, 4.30, 0, width + 3.0, depth + 3.0, 2.7, true)
  box(group, 0, 1.35, -.6, 4.8, .12, 1.7, materials.darkWood)
  box(group, 0, 1.05, .8, 3.2, .12, 1.2, materials.darkWood, false)
  ;[-1.7, 1.7].forEach(offsetX => box(group, offsetX, .82, .9, .8, .38, .8, materials.charcoal, false))
  cylinder(group, 0, 1.65, -.6, .12, .15, .25, materials.bronze, 24)
  box(group, width / 2 - .9, 2.1, -depth / 2 + .6, 1.3, 1.8, .06, materials.white, false)
  return group
}

function createCoveredBridge(parent) {
  const group = new THREE.Group()
  group.position.set(-5, 0, -.5)
  group.rotation.y = -.31
  parent.add(group)
  const length = 23.5
  box(group, 0, .46, 0, length, .42, 4.0, materials.stone)
  box(group, 0, .82, 0, length, .12, 3.1, materials.floorWood, false)
  for (let pier = 0; pier < 4; pier += 1) {
    const x = -length / 2 + 3.5 + pier * ((length - 7) / 3)
    ;[-1.4, 1.4].forEach(z => box(group, x, .03, z, 2.2, .85, .55, materials.stone, false))
  }
  for (let column = 0; column <= 8; column += 1) {
    const x = -length / 2 + .5 + column * ((length - 1) / 8)
    ;[-1.45, 1.45].forEach(z => cylinder(group, x, 2.1, z, .10, .13, 2.5, materials.darkWood, 14))
  }
  ;[-1.45, 1.45].forEach(z => {
    box(group, 0, 1.18, z, length - .5, .10, .12, materials.darkWood, false)
    for (let rail = 0; rail <= 28; rail += 1) {
      box(group, -length / 2 + .5 + rail * ((length - 1) / 28), 1.42, z, .06, .48, .07, materials.darkWood, false)
    }
    box(group, 0, 1.70, z, length - .3, .10, .16, materials.darkWood)
  })
  createGableRoof(group, 0, 3.38, 0, length + .8, 5.2, 1.8, true)
  return group
}

function createPavilion(parent, x = 8, z = 4) {
  const group = new THREE.Group()
  group.position.set(x, 0, z)
  parent.add(group)
  box(group, 0, .38, 0, 7.5, .76, 7.5, materials.stone)
  box(group, 0, .82, 0, 5.6, .12, 5.6, materials.floorWood, false)
  ;[-2.4, 2.4].forEach(offset => {
    ;[-2.4, 2.4].forEach(offsetZ => cylinder(group, offset, 2.2, offsetZ, .15, .18, 2.7, materials.darkWood, 16))
  })
  ;[-2.4, 2.4].forEach(offset => {
    ;[-2.4, 2.4].forEach(offsetZ => box(group, offset, 1.0, offsetZ, 1.8, .12, .35, materials.darkWood, false))
  })
  ;[-1, 1].forEach(side => {
    box(group, side * 2.4, 1.3, 0, .10, .55, 4.0, materials.darkWood, false)
    box(group, 0, 1.3, side * 2.4, 4.0, .55, .10, materials.darkWood, false)
  })
  createGableRoof(group, 0, 3.62, 0, 7.5, 8.0, 2.1, true)
  cylinder(group, 0, 1.35, 0, .42, .48, .16, materials.stone, 24)
  return group
}

function createBoatHouse(parent, x = 22, z = 18) {
  const group = new THREE.Group()
  group.position.set(x, 0, z)
  group.rotation.y = -.32
  parent.add(group)
  box(group, 0, .35, 0, 16.5, .70, 8.5, materials.stone)
  box(group, 0, .78, 0, 15.0, .14, 7.2, materials.floorWood, false)
  box(group, -3.8, 2.0, 0, 7.0, 2.6, 6.0, materials.plaster)
  box(group, -3.8, 3.42, 0, 7.4, .18, 6.4, materials.darkWood)
  createGableRoof(group, -3.8, 3.55, 0, 8.2, 7.5, 2.2, true)
  ;[-6.5, -1.2].forEach(frontX => {
    ;[-2.4, 2.4].forEach(z => cylinder(group, frontX, 2.3, z, .15, .18, 3.0, materials.darkWood, 16))
  })
  createGableRoof(group, -4.0, 3.82, 0, 7.5, 8.0, 1.2, true)
  box(group, 4.0, 1.9, 0, 6.5, 2.2, 6.0, materials.plaster)
  for (let column = 0; column <= 3; column += 1) {
    const frontX = 1.3 + column * 1.4
    ;[-2.5, 2.5].forEach(z => cylinder(group, frontX, 2.0, z, .12, .15, 2.4, materials.darkWood, 14))
  }
  createGableRoof(group, 4.0, 3.20, 0, 7.4, 7.0, 1.5, true)
  box(group, 7.6, .72, 0, 2.8, .10, 5.8, materials.floorWood, false)
  return group
}

function createBoundary(parent) {
  const wall = new THREE.Group()
  parent.add(wall)
  box(wall, -22, 1.5, -43, 38, 3.0, .65, materials.plaster)
  box(wall, -22, 3.1, -43, 39, .25, .95, materials.roof)
  const moon = new THREE.Mesh(trackDisposable(new THREE.CylinderGeometry(2.3, 2.3, .75, 40, 1, true)), materials.plaster)
  moon.rotation.x = Math.PI / 2
  moon.position.set(-22, 1.5, -43)
  wall.add(moon)
  box(wall, -22, 1.5, -43, .75, 3.0, .65, materials.plaster)
  box(wall, 38, 1.5, -20, .65, 3.0, 42, materials.plaster)
  box(wall, 38, 3.1, -20, .95, .25, 43, materials.roof)
  for (let index = 0; index < 7; index += 1) {
    const x = 12 + index * 4.4
    const window = box(wall, x, 2.2, -43 + .42, 1.3, 1.2, .10, materials.darkWood, false)
    window.material = materials.darkWood
    for (let bar = 0; bar < 5; bar += 1) {
      box(wall, x - .5 + bar * .25, 2.2, -43 + .49, .05, 1.0, .04, materials.darkWood, false)
    }
  }
}

function createBuildings(parent) {
  createHall(parent, -12, -28, 15, 9, 0)
  createCoveredBridge(parent)
  createPavilion(parent, 8, 4)
  createBoatHouse(parent, 22, 18)
  createBoundary(parent)
}

export function createGardenScene(canvas) {
  const renderer = new THREE.WebGLRenderer({canvas, antialias: true, preserveDrawingBuffer: true})
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.10
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap

  const scene = new THREE.Scene()
  textures = createTextures()
  materials = createMaterials(textures)
  scene.environment = textures.sky
  scene.background = textures.sky
  scene.fog = new THREE.FogExp2('#dbe7e2', .0034)

  const camera = new THREE.PerspectiveCamera(46, 1, .18, 650)
  camera.position.set(58, 36, 68)
  const controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(2, 2, -4)
  controls.enableDamping = true
  controls.dampingFactor = .06
  controls.rotateSpeed = .56
  controls.zoomSpeed = .72
  controls.panSpeed = .48
  controls.maxPolarAngle = Math.PI / 2.06
  controls.minPolarAngle = .08

  const hemisphere = new THREE.HemisphereLight('#dfeaff', '#7f8d6d', 1.30)
  scene.add(hemisphere)
  const sunDirection = new THREE.Vector3(.38, .68, .55).normalize()
  const sunlight = new THREE.DirectionalLight('#fff4da', 2.55)
  sunlight.position.copy(sunDirection).multiplyScalar(65)
  sunlight.castShadow = true
  sunlight.shadow.mapSize.set(2048, 2048)
  sunlight.shadow.camera.near = 10
  sunlight.shadow.camera.far = 190
  sunlight.shadow.camera.left = -80
  sunlight.shadow.camera.right = 80
  sunlight.shadow.camera.top = 75
  sunlight.shadow.camera.bottom = -70
  sunlight.shadow.bias = -.00045
  scene.add(sunlight)
  const fillLight = new THREE.DirectionalLight('#cadcf5', .46)
  fillLight.position.set(-55, 28, -35)
  scene.add(fillLight)

  const skyDome = new THREE.Mesh(trackDisposable(new THREE.SphereGeometry(420, 64, 40)), materials.sky)
  scene.add(skyDome)
  const water = createLandscape(scene)
  createBuildings(scene)

  const clock = new THREE.Clock()
  let animationId = 0

  function resize() {
    if (!canvas.clientWidth || !canvas.clientHeight) return
    const width = canvas.clientWidth
    const height = canvas.clientHeight
    renderer.setSize(width, height, false)
    camera.aspect = width / height
    camera.updateProjectionMatrix()
  }

  function update() {
    animationId = requestAnimationFrame(update)
    const delta = Math.min(clock.getDelta(), .05)
    const elapsed = clock.getElapsedTime()
    if (waterMaterial?.normalMap) {
      waterMaterial.normalMap.offset.set(
        Math.sin(elapsed * .20) * .045,
        elapsed * .028
      )
      waterMaterial.needsUpdate = true
    }
    swayMeshes.forEach(mesh => {
      mesh.rotation.z = mesh.userData.baseRotationZ || (mesh.userData.baseRotationZ = mesh.rotation.z)
      mesh.rotation.z = mesh.userData.baseRotationZ + Math.sin(elapsed * 1.05 + mesh.position.x * .45 + mesh.position.y) * .017
    })
    controls.update()
    renderer.render(scene, camera)
  }

  function dispose() {
    if (animationId) cancelAnimationFrame(animationId)
    controls?.dispose()
    scene.traverse(child => {
      child.geometry?.dispose()
      const childMaterials = Array.isArray(child.material) ? child.material : [child.material]
      childMaterials.forEach(material => material?.dispose())
    })
    disposables.textures.forEach(texture => texture.dispose())
    disposables.materials.forEach(material => material.dispose())
    disposables.geometries.forEach(geometry => geometry.dispose())
    renderer?.dispose()
    renderer?.forceContextLoss()
    swayMeshes.length = 0
  }

  resize()
  update()

  return {renderer, scene, camera, controls, resize, dispose}
}
