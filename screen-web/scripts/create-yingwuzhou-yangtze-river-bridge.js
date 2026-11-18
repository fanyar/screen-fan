import {mkdir, writeFile} from 'node:fs/promises';
import path from 'node:path';
import * as THREE from 'three';
import {GLTFExporter} from 'three/examples/jsm/exporters/GLTFExporter.js';

const output = process.argv[2] || 'public/platform/models/yingwuzhou-yangtze-river-bridge.glb';

globalThis.FileReader = class FileReader {
  readAsArrayBuffer(blob) {
    blob.arrayBuffer().then(buffer => {
      this.result = buffer;
      this.onloadend?.();
    });
  }
};

const boxGeometry = new THREE.BoxGeometry(1, 1, 1);

const CABLE = {
  westAnchorX: -118,
  eastAnchorX: 118,
  anchorY: 2.2,
  towerX: 65,
  towerY: 24.5,
  midY: 8,
  hangerBottomY: 0.5
};

const materials = {
  water: new THREE.MeshStandardMaterial({
    color: 0x1e5f9c,
    roughness: 0.15,
    metalness: 0.08,
    transparent: true,
    opacity: 0.9
  }),
  deck: new THREE.MeshStandardMaterial({
    color: 0xd94f1e,
    roughness: 0.55,
    metalness: 0.2
  }),
  asphalt: new THREE.MeshStandardMaterial({color: 0x33373d, roughness: 0.86, metalness: 0.02}),
  walkway: new THREE.MeshStandardMaterial({color: 0xd8d3c8, roughness: 0.76, metalness: 0.02}),
  pylon: new THREE.MeshStandardMaterial({color: 0x9aa2ab, roughness: 0.72, metalness: 0.08}),
  cable: new THREE.MeshStandardMaterial({
    color: 0xe8edf4,
    roughness: 0.28,
    metalness: 0.75
  }),
  girder: new THREE.MeshStandardMaterial({
    color: 0xc74318,
    roughness: 0.5,
    metalness: 0.28
  })
};

function addBox(parent, name, size, position, material, rotation = [0, 0, 0]) {
  const mesh = new THREE.Mesh(boxGeometry, material);
  mesh.name = name;
  mesh.scale.set(size[0], size[1], size[2]);
  mesh.position.set(position[0], position[1], position[2]);
  mesh.rotation.set(rotation[0], rotation[1], rotation[2]);
  parent.add(mesh);
  return mesh;
}

function addTube(parent, name, curve, radius, material) {
  const mesh = new THREE.Mesh(
      new THREE.TubeGeometry(curve, 180, radius, 10, false),
      material
  );
  mesh.name = name;
  parent.add(mesh);
  return mesh;
}

class MainCableCurve extends THREE.Curve {
  constructor(z) {
    super();
    this.z = z;
  }

  getPoint(t, target = new THREE.Vector3()) {
    const x = CABLE.westAnchorX + (CABLE.eastAnchorX - CABLE.westAnchorX) * t;
    return target.set(x, cableYAt(x), this.z);
  }
}

function createRiver(parent) {
  const river = new THREE.Group();
  river.name = 'Yangtze-River';
  addBox(river, 'River-Surface', [330, 0.2, 88], [0, -3.2, 0], materials.water);
  parent.add(river);
}

function createDeck(parent) {
  const deck = new THREE.Group();
  deck.name = 'Orange-Steel-Deck';

  addBox(deck, 'Steel-Box-Girder', [230, 1.3, 12], [0, -0.4, 0], materials.deck);
  addBox(deck, 'Road-Surface', [230, 0.25, 10.5], [0, 0.4, 0], materials.asphalt);
  addBox(deck, 'Center-Barrier', [230, 0.55, 0.28], [0, 0.8, 0], materials.deck);

  for (const [index, z] of [-5.4, 5.4].entries()) {
    const side = index === 0 ? 'South' : 'North';
    addBox(deck, `Edge-Girder-${side}`, [230, 1.6, 0.45], [0, -0.2, z], materials.girder);
    addBox(
        deck,
        `Walkway-${side}`,
        [230, 0.3, 0.8],
        [0, 0.65, index === 0 ? z - 0.5 : z + 0.5],
        materials.walkway
    );
  }

  for (let x = -112; x <= 112; x += 8) {
    const name = String(x).replaceAll('.', '_');
    addBox(deck, `Deck-Cross-Beam-${name}`, [0.3, 0.5, 12], [x, -0.7, 0], materials.girder);
  }

  parent.add(deck);
}

function createTower(parent, x, name) {
  const tower = new THREE.Group();
  tower.name = name;
  tower.position.x = x;

  addBox(tower, `${name}-Foundation`, [10, 3, 20], [0, -5.8, 0], materials.pylon);

  for (const [index, z] of [-5.5, 5.5].entries()) {
    const side = index === 0 ? 'South' : 'North';
    addBox(tower, `${name}-Column-${side}`, [2.4, 29, 2.4], [0, 11, z], materials.pylon);
  }

  for (const [index, y] of [4, 11, 18, 23].entries()) {
    addBox(tower, `${name}-Cross-Beam-${index + 1}`, [2, 1.3, 11], [0, y, 0], materials.pylon);
  }

  addBox(tower, `${name}-Top-Beam`, [2.4, 1.5, 12.5], [0, 25.5, 0], materials.pylon);
  parent.add(tower);
}

function createTowers(parent) {
  const towers = new THREE.Group();
  towers.name = 'Three-Pylons';
  createTower(towers, -65, 'West-Pylon');
  createTower(towers, 0, 'Central-Pylon');
  createTower(towers, 65, 'East-Pylon');
  parent.add(towers);
}

function cableYAt(x) {
  const westTowerX = -CABLE.towerX;
  const centralTowerX = 0;
  const eastTowerX = CABLE.towerX;

  if (x < westTowerX) {
    const progress = (x - CABLE.westAnchorX) / (westTowerX - CABLE.westAnchorX);
    return CABLE.anchorY + (CABLE.towerY - CABLE.anchorY) * progress;
  }

  if (x > eastTowerX) {
    const progress = (x - eastTowerX) / (CABLE.eastAnchorX - eastTowerX);
    return CABLE.towerY + (CABLE.anchorY - CABLE.towerY) * progress;
  }

  const startTowerX = x < centralTowerX ? westTowerX : centralTowerX;
  const endTowerX = x < centralTowerX ? centralTowerX : eastTowerX;
  const progress = (x - startTowerX) / (endTowerX - startTowerX);
  return CABLE.towerY - 4 * (CABLE.towerY - CABLE.midY) * progress * (1 - progress);
}

function createMainCables(parent) {
  const cables = new THREE.Group();
  cables.name = 'Main-Cables';

  for (const [index, z] of [-5.5, 5.5].entries()) {
    const side = index === 0 ? 'South' : 'North';
    const curve = new MainCableCurve(z);
    addTube(cables, `Main-Cable-${side}`, curve, 0.32, materials.cable);
  }

  parent.add(cables);
}

function createHangers(parent) {
  const hangers = new THREE.Group();
  hangers.name = 'Suspender-Cables';

  for (const [sideIndex, z] of [-5.5, 5.5].entries()) {
    const side = sideIndex === 0 ? 'South' : 'North';
    let number = 1;

    const hangerSpans = [
      {name: 'West-Side', start: -111, end: -71, step: 5},
      {name: 'West-Main', start: -59, end: -6, step: 6.5},
      {name: 'East-Main', start: 6, end: 59, step: 6.5},
      {name: 'East-Side', start: 71, end: 111, step: 5}
    ];

    for (const span of hangerSpans) {
      for (let x = span.start; x <= span.end + 0.01; x += span.step) {
        const topY = cableYAt(x);
        const bottomY = CABLE.hangerBottomY;
        const length = topY - bottomY;
        addBox(
            hangers,
            `Hanger-${span.name}-${side}-${String(number).padStart(2, '0')}`,
            [0.11, length, 0.11],
            [x, (topY + bottomY) / 2, z],
            materials.cable
        );
        number += 1;
      }
    }
  }

  parent.add(hangers);
}

function createAnchoragesAndApproaches(parent) {
  const group = new THREE.Group();
  group.name = 'Anchorages-And-Approaches';

  for (const [index, direction] of [['West', -1], ['East', 1]].entries()) {
    const side = index === 0 ? 'West' : 'East';
    const anchorX = direction * 119;
    addBox(group, `Anchorage-${side}`, [12, 5.5, 16], [anchorX, 1.2, 0], materials.pylon);
    addBox(group, `Road-Approach-${side}`, [42, 0.8, 11], [direction * 152, 0.2, 0], materials.asphalt);
    addBox(group, `Approach-Pier-${side}-1`, [2, 5.5, 6], [direction * 142, -2.4, 0], materials.pylon);
    addBox(group, `Approach-Pier-${side}-2`, [2, 5.5, 6], [direction * 162, -2.4, 0], materials.pylon);
  }

  parent.add(group);
}

function createYingwuzhouYangtzeRiverBridge() {
  const bridge = new THREE.Group();
  bridge.name = 'Yingwuzhou-Yangtze-River-Bridge';

  createRiver(bridge);
  createDeck(bridge);
  createTowers(bridge);
  createMainCables(bridge);
  createHangers(bridge);
  createAnchoragesAndApproaches(bridge);

  return bridge;
}

const exporter = new GLTFExporter();
const bridge = createYingwuzhouYangtzeRiverBridge();
const glb = await exporter.parseAsync(bridge, {binary: true});

await mkdir(path.dirname(output), {recursive: true});
await writeFile(output, Buffer.from(glb));
console.log(`${output}: ${glb.byteLength} bytes`);
