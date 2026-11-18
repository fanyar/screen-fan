import * as THREE from 'three'
import {VILLA_VIEWS} from '@/const/villa'

const FLOOR_BASES = {one: 0.35, two: 3.65, three: 6.95}
let materials
let textures
let roomSets

export function box(parent, x, y, z, width, height, depth, material, cast = true) {
  const mesh = new THREE.Mesh(new THREE.BoxGeometry(width, height, depth), material)
  mesh.position.set(x, y, z)
  mesh.castShadow = cast
  mesh.receiveShadow = true
  parent.add(mesh)
  return mesh
}

export function cylinder(parent, x, y, z, radiusTop, radiusBottom, height, material, segments = 24) {
  const mesh = new THREE.Mesh(new THREE.CylinderGeometry(radiusTop, radiusBottom, height, segments), material)
  mesh.position.set(x, y, z)
  mesh.castShadow = true
  mesh.receiveShadow = true
  parent.add(mesh)
  return mesh
}

function wallSegment(parent, axis, from, to, fixed, base, top, material, thickness = .18) {
  const length = Math.abs(to - from)
  const center = (from + to) / 2
  const height = top - base
  if (length <= .01 || height <= .01) return
  if (axis === 'x') {
    box(parent, center, base + height / 2, fixed, length, height, thickness, material)
  } else {
    box(parent, fixed, base + height / 2, center, thickness, height, length, material)
  }
}

function roomWall(parent, axis, from, to, fixed, floorBase, options = {}) {
  const top = floorBase + 3
  const opening = options.opening
  if (!opening) {
    wallSegment(parent, axis, from, to, fixed, floorBase + .12, top, materials.plaster)
    return
  }
  const [start, end, sill, head] = opening
  wallSegment(parent, axis, from, start, fixed, floorBase + .12, top, materials.plaster)
  wallSegment(parent, axis, end, to, fixed, floorBase + .12, top, materials.plaster)
  wallSegment(parent, axis, start, end, fixed, floorBase + .12, floorBase + sill, materials.plaster)
  wallSegment(parent, axis, start, end, fixed, floorBase + head, top, materials.plaster)
  const center = (start + end) / 2
  const size = end - start
  const glass = box(parent, axis === 'x' ? center : fixed, floorBase + (sill + head) / 2,
    axis === 'x' ? fixed : center, axis === 'x' ? size : .06, head - sill, axis === 'x' ? .06 : size, materials.glass, false)
  glass.material = new THREE.MeshStandardMaterial({
    map: textures.window, roughness: .18, metalness: .05, side: THREE.DoubleSide
  })
  if (axis === 'x') {
    box(parent, start, floorBase + (sill + head) / 2, fixed, .08, head - sill, .2, materials.darkWood)
    box(parent, end, floorBase + (sill + head) / 2, fixed, .08, head - sill, .2, materials.darkWood)
  } else {
    box(parent, fixed, floorBase + (sill + head) / 2, start, .2, head - sill, .08, materials.darkWood)
    box(parent, fixed, floorBase + (sill + head) / 2, end, .2, head - sill, .08, materials.darkWood)
  }
}

function baseboard(parent, centerX, baseY, centerZ, width, depth) {
  box(parent, centerX, baseY + .2, centerZ + depth / 2, width, .1, .08, materials.darkWood, false)
}

function cushion(parent, x, y, z, width, height, depth, material) {
  const mesh = box(parent, x, y, z, width, height, depth, material)
  mesh.rotation.y = Math.random() * .12 - .06
}

function addRug(parent, x, y, z, width, depth) {
  const rug = box(parent, x, y + .07, z, width, .03, depth, materials.fabric, false)
  rug.rotation.y = .04
}

function createBed(parent, x, z, floorBase, width = 2.0, material = materials.linen) {
  box(parent, x, floorBase + .68, z - .8, width + .1, 1.15, .14, materials.darkWood)
  box(parent, x, floorBase + .38, z, width, .42, 2.2, materials.darkWood)
  box(parent, x, floorBase + .65, z + .15, width - .1, .2, 1.9, material)
  cushion(parent, x, floorBase + .8, z - .3, width - .25, .22, .45, materials.white)
  cushion(parent, x - width / 4, floorBase + .82, z - .28, (width - .5) / 2, .2, .4, materials.fabric)
  cushion(parent, x + width / 4, floorBase + .82, z - .28, (width - .5) / 2, .2, .4, materials.fabric)
  box(parent, x - width / 2 - .45, floorBase + .72, z - .7, .5, .45, .45, materials.darkWood)
  cylinder(parent, x - width / 2 - .45, floorBase + 1.1, z - .7, .16, .16, .24, materials.bronze, 18)
  box(parent, x + width / 2 + .45, floorBase + .72, z - .7, .5, .45, .45, materials.darkWood)
  addRug(parent, x, floorBase, z + 1.5, width + 1.4, 1.6)
}

function createSofa(parent, x, z, floorBase, angle = 0) {
  const group = new THREE.Group()
  group.position.set(x, floorBase, z)
  group.rotation.y = angle
  box(group, 0, .42, 0, 2.7, .5, 1.0, materials.fabric)
  box(group, 0, .78, -.38, 2.7, .68, .22, materials.fabric)
  cushion(group, -.75, .68, .08, 1.05, .16, .68, materials.linen)
  cushion(group, .75, .68, .08, 1.05, .16, .68, materials.linen)
  cushion(group, 0, .62, .08, 1.0, .16, .68, materials.charcoal)
  ;[-1.2, 1.2].forEach(offset => cylinder(group, offset, .17, .38, .05, .07, .34, materials.darkWood, 14))
  parent.add(group)
  return group
}

function createLowTable(parent, x, z, floorBase) {
  box(parent, x, floorBase + .48, z, 1.7, .08, .9, materials.darkWood)
  cylinder(parent, x - .6, floorBase + .24, z, .04, .05, .48, materials.darkMetal, 10)
  cylinder(parent, x + .6, floorBase + .24, z, .04, .05, .48, materials.darkMetal, 10)
  cylinder(parent, x + .2, floorBase + .6, z, .16, .13, .1, materials.bronze, 20)
}

function createTeaSet(parent, x, z, floorBase) {
  box(parent, x, floorBase + .62, z, 1.6, .09, .78, materials.darkWood)
  cylinder(parent, x - .3, floorBase + .76, z, .12, .08, .16, materials.white, 20)
  cylinder(parent, x + .3, floorBase + .73, z, .07, .05, .09, materials.white, 18)
  cylinder(parent, x + .1, floorBase + .72, z + .18, .05, .04, .05, materials.white, 16)
}

function createShelf(parent, x, z, floorBase, width, depth = .4) {
  const height = 2.8
  box(parent, x, floorBase + height / 2 + .2, z, width, height, depth, materials.darkWood)
  for (let index = 0; index < 4; index += 1) {
    box(parent, x, floorBase + .65 + index * .65, z, width - .1, .05, depth - .06, materials.wood, false)
    for (let book = 0; book < Math.floor(width * 2); book += 1) {
      if (Math.random() > .55) continue
      box(parent, x - width / 2 + .3 + book * .44, floorBase + .8 + index * .65, z,
        .2 + Math.random() * .08, .4, depth - .14, index % 2 ? materials.charcoal : materials.bronze, false)
    }
  }
}

function buildRoomShell(config) {
  const group = new THREE.Group()
  const [x1, z1, x2, z2] = config.bounds
  const width = x2 - x1
  const depth = z2 - z1
  const centerX = (x1 + x2) / 2
  const centerZ = (z1 + z2) / 2
  const floorBase = FLOOR_BASES[config.level]
  const isTerrace = config.roomId === 'f3-terrace'

  const floorMaterial = isTerrace ? materials.wood : materials.marble
  box(group, centerX, floorBase + .07, centerZ, width, .14, depth, floorMaterial)
  if (!isTerrace) {
    box(group, centerX, floorBase + 2.92, centerZ, width, .16, depth, materials.white, false)
    box(group, centerX, floorBase + 2.76, centerZ, width - .2, .05, depth - .2, materials.darkWood, false)
  } else {
    box(group, centerX, floorBase + .95, z2, width, .16, .1, materials.darkWood, false)
    for (let index = 0; index < Math.floor(width * 3); index += 1) {
      const offset = -width / 2 + (width / (Math.floor(width * 3) - 1)) * index
      box(group, centerX + offset, floorBase + .45, z2, .06, .7, .08, materials.darkWood)
    }
  }

  const openings = config.openings || []
  openings.forEach(([axis, from, to, fixed, start, end, sill, head]) => {
    roomWall(group, axis, from, to, fixed, floorBase, {opening: [start, end, sill, head]})
  })
  if (config.closedWalls?.includes('west')) wallSegment(group, 'z', z1, z2, x1, floorBase + .12, floorBase + 3, materials.plaster)
  if (config.closedWalls?.includes('east')) wallSegment(group, 'z', z1, z2, x2, floorBase + .12, floorBase + 3, materials.plaster)
  if (config.closedWalls?.includes('north')) wallSegment(group, 'x', x1, x2, z1, floorBase + .12, floorBase + 3, materials.plaster)
  if (config.closedWalls?.includes('south')) wallSegment(group, 'x', x1, x2, z2, floorBase + .12, floorBase + 3, materials.plaster)

  baseboard(group, centerX, floorBase, centerZ, width, depth)
  const roomLight = new THREE.PointLight('#fff3dd', .78, 9, 2)
  roomLight.position.set(centerX, floorBase + 2.4, centerZ)
  group.add(roomLight)
  return group
}

function furnishRoom(config) {
  const group = roomSets.get(config.roomId)
  const base = FLOOR_BASES[config.level]
  const x = config.focus[0]
  const z = config.focus[1]
  switch (config.roomId) {
    case 'f1-foyer':
      box(group, -7.8, base + 1.0, 2.6, .18, 2.0, 2.4, materials.darkWood)
      for (let index = 0; index < 10; index += 1) {
        box(group, -7.8 + (index / 9 - .5) * 1.9, base + 1.4, 2.6, .06, 1.5, .08, materials.darkWood)
      }
      cylinder(group, -6.6, base + .42, 3.3, .35, .4, .65, materials.jade, 24)
      box(group, -5.5, base + .28, 3.2, .9, .36, .35, materials.darkWood)
      break
    case 'f1-living':
      createSofa(group, -2.7, 1.7, base, Math.PI)
      createLowTable(group, -2.6, 3.1, base)
      box(group, -2.6, base + 1.2, .5, 3.1, 1.7, .12, materials.darkWood)
      box(group, -2.6, base + 1.1, .54, 2.5, 1.3, .05, materials.charcoal, false)
      cylinder(group, -.7, base + .35, 3.7, .24, .3, .6, materials.bronze, 20)
      break
    case 'f1-tea':
      box(group, -6.6, base + .18, -.8, 3.1, .16, 2.6, materials.wood)
      createTeaSet(group, -6.6, -.8, base)
      cushion(group, -7.4, base + .38, -.4, .8, .22, .8, materials.fabric)
      cushion(group, -5.8, base + .38, -1.2, .8, .22, .8, materials.fabric)
      for (let index = 0; index < 12; index += 1) {
        box(group, -7.9, base + .45 + index * .18, -.8 + (index / 11 - .5) * 2.1, .1, .1, .08, materials.darkWood)
      }
      break
    case 'f1-dining':
      box(group, -2.6, base + .82, -.8, 2.7, .1, 1.2, materials.darkWood)
      ;[-3.5, -1.7].forEach(offset => cylinder(group, offset, base + .4, -.8, .08, .1, .8, materials.darkWood, 12))
      ;[-3.5, -2.6, -1.7].forEach(offsetX => {
        cushion(group, offsetX, base + .48, -1.6, .42, .5, .42, materials.fabric)
        cushion(group, offsetX, base + .48, 0, .42, .5, .42, materials.fabric)
      })
      cylinder(group, -2.6, base + 2.2, -.8, .06, .12, .5, materials.charcoal, 16)
      box(group, -.2, base + 1.0, -.8, .4, 1.7, 2.1, materials.darkWood)
      break
    case 'f1-kitchen':
      box(group, -2.8, base + .48, -4.6, 3.8, .8, .7, materials.charcoal)
      box(group, -2.8, base + .92, -4.6, 3.7, .08, .66, materials.marble)
      box(group, -2.8, base + 1.7, -4.6, 3.5, .75, .5, materials.charcoal)
      box(group, -2.5, base + .58, -2.6, 2.2, .8, 1.2, materials.charcoal)
      box(group, -2.5, base + 1.0, -2.6, 2.1, .09, 1.1, materials.marble)
      ;[-3.2, -1.8].forEach(offset => cylinder(group, offset, base + .42, -1.8, .2, .24, .5, materials.wood, 16))
      break
    case 'f1-guest':
      createBed(group, -6.6, -3.3, base, 1.7)
      box(group, -7.8, base + 1.1, -3.2, .4, 2.2, 2.0, materials.darkWood)
      cushion(group, -5.3, base + .55, -2.2, .7, .5, .7, materials.fabric)
      break
    case 'f2-master':
      createBed(group, -6.2, 2.2, base, 2.1)
      box(group, -4.4, base + 1.2, 2.0, .4, 2.4, 2.6, materials.darkWood)
      createSofa(group, -5.6, 3.2, base, 0)
      cylinder(group, -7.7, base + .8, 3.2, .2, .25, 1.2, materials.darkWood, 18)
      break
    case 'f2-bedroom':
      createBed(group, -2.1, 2.2, base, 1.6)
      box(group, -.2, base + 1.1, 2.0, .4, 2.0, 2.2, materials.darkWood)
      cushion(group, -3.5, base + .5, 3.0, .7, .45, .7, materials.fabric)
      break
    case 'f2-family':
      createSofa(group, -6.1, -.3, base, 0)
      createLowTable(group, -6.0, .9, base)
      box(group, -6.1, base + 1.1, -2.4, 3.3, 1.8, .12, materials.darkWood)
      box(group, -6.1, base + 1.0, -2.36, 2.7, 1.35, .04, materials.charcoal, false)
      break
    case 'f2-study':
      box(group, -2.0, base + .78, -1.5, 2.2, .1, 1.0, materials.darkWood)
      ;[-2.8, -1.2].forEach(offset => cylinder(group, offset, base + .38, -1.5, .05, .06, .76, materials.darkMetal, 12))
      cushion(group, -2.0, base + .5, -.2, .55, .55, .55, materials.fabric)
      createShelf(group, -2.0, -2.3, base, 3.0)
      cylinder(group, -2.8, base + 1.0, -.8, .14, .18, .25, materials.bronze, 20)
      break
    case 'f2-secondary':
      createBed(group, -4.0, -3.3, base, 1.7)
      box(group, -2.2, base + 1.2, -3.4, .35, 2.4, 2.8, materials.darkWood)
      cushion(group, -5.7, base + .5, -2.3, .8, .45, .8, materials.linen)
      break
    case 'f3-tea':
      box(group, -6.1, base + .18, 1.8, 3.5, .18, 2.2, materials.wood)
      box(group, -6.1, base + .94, 1.8, 2.4, .1, .8, materials.darkWood)
      cushion(group, -7.0, base + .4, 2.4, .9, .24, .8, materials.fabric)
      cushion(group, -5.2, base + .4, 1.2, .9, .24, .8, materials.fabric)
      createShelf(group, -6.1, .3, base, 3.2)
      cylinder(group, -4.6, base + 1.5, 2.8, .14, .18, .3, materials.bronze, 20)
      break
    case 'f3-guest':
      createBed(group, -2.0, 1.9, base, 1.6)
      box(group, -.2, base + 1.1, 1.8, .4, 2.1, 2.2, materials.darkWood)
      cushion(group, -3.5, base + .5, 2.7, .7, .45, .7, materials.fabric)
      break
    case 'f3-activity':
      createSofa(group, -6.0, -.6, base, 0)
      box(group, -6.0, base + .75, .7, 1.9, .3, .8, materials.darkWood)
      box(group, -6.0, base + 1.2, -2.5, 3.4, 1.8, .12, materials.darkWood)
      box(group, -6.0, base + 1.1, -2.46, 2.8, 1.35, .04, materials.charcoal, false)
      cylinder(group, -4.4, base + .28, .8, .34, .36, .18, materials.jade, 22)
      break
    case 'f3-terrace':
      ;[-2.9, -1.1].forEach((offsetX, index) => {
        const lounge = box(group, offsetX, base + .38, -.8, 1.5, .18, .75, materials.fabric)
        lounge.rotation.y = index ? -.16 : .16
        box(group, offsetX - .4, base + .52, -.35, .7, .16, .6, materials.linen)
      })
      cylinder(group, -2.0, base + .42, .9, .4, .44, .12, materials.darkWood, 22)
      box(group, -2.0, base + .58, 1.5, 1.6, .4, .7, materials.wood)
      ;[-3.6, -.4].forEach(offset => cylinder(group, offset, base + .4, 1.7, .24, .28, .55, materials.jade, 18))
      break
    default:
      break
  }
}

export function createRoomSets(scene, assetMaterials, assetTextures) {
  materials = assetMaterials
  textures = assetTextures
  const sets = new Map()
  roomSets = sets
  const roomConfigs = VILLA_VIEWS.filter(view => view.roomId).map(view => ({
    roomId: view.roomId,
    level: view.group === '一层' ? 'one' : view.group === '二层' ? 'two' : 'three',
    bounds: {
      'f1-foyer': [-8, .8, -5.1, 4.2],
      'f1-living': [-5.1, .5, 0, 4.2],
      'f1-tea': [-8, -2.2, -5, .5],
      'f1-dining': [-5, -2.2, 0, .5],
      'f1-kitchen': [-5, -5, 0, -2.2],
      'f1-guest': [-8, -5, -5, -2.2],
      'f2-master': [-8, .5, -4.2, 4],
      'f2-bedroom': [-4.2, .5, 0, 4],
      'f2-family': [-8, -2.2, -4.2, .5],
      'f2-study': [-4.2, -2.2, 0, .5],
      'f2-secondary': [-8, -5, 0, -2.2],
      'f3-tea': [-8, .3, -4, 3.5],
      'f3-guest': [-4, .3, 0, 3.5],
      'f3-activity': [-8, -2.7, -4, .3],
      'f3-terrace': [-4, -2.7, 0, .3]
    }[view.roomId],
    closedWalls: {
      'f1-foyer': ['west', 'north'],
      'f1-living': ['north'],
      'f1-tea': ['west', 'north'],
      'f1-dining': ['north'],
      'f1-kitchen': ['east', 'north'],
      'f1-guest': ['west', 'north'],
      'f2-master': ['west', 'north'],
      'f2-bedroom': ['north'],
      'f2-family': ['west', 'north'],
      'f2-study': ['east', 'north'],
      'f2-secondary': ['north'],
      'f3-tea': ['west', 'north'],
      'f3-guest': ['east', 'north'],
      'f3-activity': ['west', 'north'],
      'f3-terrace': []
    }[view.roomId],
    openings: {
      'f1-foyer': [['x', -8, -5.1, 4.2, -7.2, -6, .9, 2.2]],
      'f1-living': [],
      'f1-tea': [['x', -8, -5, .5, -7.4, -5.7, .7, 2.3]],
      'f1-dining': [['x', -5, 0, .5, -3.8, -1.6, .6, 2.5]],
      'f1-kitchen': [['z', -5, -2.2, 0, -3.7, -2.9, .8, 2.3]],
      'f1-guest': [['z', -5, -2.2, -8, -4.3, -3.0, .7, 2.3]],
      'f2-master': [['x', -8, -4.2, 4, -7.3, -5.0, .6, 2.7]],
      'f2-bedroom': [['x', -4.2, 0, 4, -3.5, -1.0, .6, 2.7]],
      'f2-family': [['x', -8, -4.2, .5, -7.0, -5.1, .6, 2.6]],
      'f2-study': [['x', -4.2, 0, .5, -3.4, -1.1, .7, 2.6]],
      'f2-secondary': [['x', -8, 0, -5, -6.2, -2.2, .65, 2.4]],
      'f3-tea': [['x', -8, -4, 3.5, -7.2, -4.8, .5, 2.9]],
      'f3-guest': [['x', -4, 0, 3.5, -3.2, -.8, .6, 2.6]],
      'f3-activity': [['x', -8, -4, .3, -7.2, -4.8, .6, 2.6]],
      'f3-terrace': []
    }[view.roomId],
    focus: {
      'f1-foyer': [-6.7, 2.5],
      'f1-living': [-2.6, 2.4],
      'f1-tea': [-6.6, -.8],
      'f1-dining': [-2.6, -.8],
      'f1-kitchen': [-2.5, -3.4],
      'f1-guest': [-6.6, -3.4],
      'f2-master': [-6.2, 2.3],
      'f2-bedroom': [-2.1, 2.3],
      'f2-family': [-6.1, -.8],
      'f2-study': [-2.0, -.8],
      'f2-secondary': [-4.0, -3.5],
      'f3-tea': [-6.1, 1.9],
      'f3-guest': [-2.0, 1.9],
      'f3-activity': [-6.0, -1.3],
      'f3-terrace': [-2.0, -1.3]
    }[view.roomId]
  }))

  roomConfigs.forEach(config => {
    const group = buildRoomShell(config)
    sets.set(config.roomId, group)
    furnishRoom(config)
    group.visible = false
    scene.add(group)
  })
  return sets
}
