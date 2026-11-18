import {mkdir, writeFile} from 'node:fs/promises';
import path from 'node:path';
import * as THREE from 'three';
import {GLTFExporter} from 'three/examples/jsm/exporters/GLTFExporter.js';

const output = process.argv[2] || 'public/platform/models/hankou-changfeng-bridge.glb';

globalThis.FileReader = class FileReader {
  readAsArrayBuffer(blob) {
    blob.arrayBuffer().then(buffer => {
      this.result = buffer;
      this.onloadend?.();
    });
  }
};

const boxGeometry = new THREE.BoxGeometry(1, 1, 1);

const ARCH = {
  sideEndX: 186,
  springingX: 126,
  springingY: 4,
  crownY: 54,
  sideCrownY: 20,
  hangerBottomY: 0.8
};

const materials = {
  water: new THREE.MeshStandardMaterial({
    color: 0x22689d,
    roughness: 0.16,
    metalness: 0.08,
    transparent: true,
    opacity: 0.9
  }),
  arch: new THREE.MeshStandardMaterial({
    color: 0x2f80ed,
    roughness: 0.45,
    metalness: 0.35
  }),
  deck: new THREE.MeshStandardMaterial({
    color: 0x5b9bf5,
    roughness: 0.68,
    metalness: 0.2
  }),
  asphalt: new THREE.MeshStandardMaterial({color: 0x33373d, roughness: 0.86, metalness: 0.02}),
  walkway: new THREE.MeshStandardMaterial({color: 0xd8d3c8, roughness: 0.76, metalness: 0.02}),
  hanger: new THREE.MeshStandardMaterial({
    color: 0xe8edf4,
    roughness: 0.28,
    metalness: 0.72
  }),
  pier: new THREE.MeshStandardMaterial({color: 0x9aa2ab, roughness: 0.72, metalness: 0.08})
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

function archYAt(x) {
  const absoluteX = Math.abs(x);
  if (absoluteX <= ARCH.springingX) {
    return ARCH.springingY + (ARCH.crownY - ARCH.springingY)
        * (1 - (absoluteX / ARCH.springingX) ** 2);
  }

  const progress = (absoluteX - ARCH.springingX) / (ARCH.sideEndX - ARCH.springingX);
  return ARCH.springingY
      + (ARCH.sideCrownY - ARCH.springingY) * 4 * progress * (1 - progress);
}

class ArchRibCurve extends THREE.Curve {
  constructor(z) {
    super();
    this.z = z;
  }

  getPoint(t, target = new THREE.Vector3()) {
    const x = -ARCH.sideEndX + ARCH.sideEndX * 2 * t;
    return target.set(x, archYAt(x), this.z);
  }
}

function createRiver(parent) {
  const river = new THREE.Group();
  river.name = 'River';
  addBox(river, 'River-Surface', [540, 0.2, 140], [0, -3.2, 0], materials.water);
  parent.add(river);
}

function createDeck(parent) {
  const deck = new THREE.Group();
  deck.name = 'Tied-Steel-Deck';

  addBox(deck, 'Deck-Girder', [372, 1.4, 27], [0, -0.2, 0], materials.deck);
  addBox(deck, 'Road-Surface', [372, 0.28, 24], [0, 0.48, 0], materials.asphalt);
  addBox(deck, 'Center-Barrier', [372, 0.65, 0.3], [0, 0.85, 0], materials.deck);

  for (const z of [-8, -4, 4, 8]) {
    addBox(deck, `Lane-Marking-${String(z).replace('-', 'M')}`, [372, 0.04, 0.18], [0, 0.63, z], materials.walkway);
  }

  for (const [index, z] of [-13.2, 13.2].entries()) {
    const side = index === 0 ? 'South' : 'North';
    addBox(deck, `Tie-Girder-${side}`, [372, 1.8, 0.6], [0, -0.1, z], materials.arch);
    addBox(
        deck,
        `Walkway-${side}`,
        [372, 0.3, 1.2],
        [0, 0.68, index === 0 ? z - 0.8 : z + 0.8],
        materials.walkway
    );
  }

  for (let x = -180; x <= 180; x += 12) {
    const name = String(x).replaceAll('.', '_');
    addBox(deck, `Floor-Beam-${name}`, [0.45, 0.7, 27], [x, -0.75, 0], materials.arch);
  }

  parent.add(deck);
}

function createArchRibs(parent) {
  const arch = new THREE.Group();
  arch.name = 'Flybird-Arch-Ribs';

  for (const [index, z] of [-12.8, 12.8].entries()) {
    const side = index === 0 ? 'South' : 'North';
    const rib = new THREE.Mesh(
        new THREE.TubeGeometry(new ArchRibCurve(z), 220, 0.9, 16, false),
        materials.arch
    );
    rib.name = `Arch-Rib-${side}`;
    arch.add(rib);
  }

  for (const x of [-156, -100, -50, 0, 50, 100, 156]) {
    addBox(
        arch,
        `Arch-Cross-Brace-${String(x).replace('-', 'M')}`,
        [0.6, 0.6, 25.6],
        [x, archYAt(x), 0],
        materials.arch
    );
  }

  parent.add(arch);
}

function createHangers(parent) {
  const hangers = new THREE.Group();
  hangers.name = 'Arch-Hangers';

  for (const [sideIndex, z] of [-12.8, 12.8].entries()) {
    const side = sideIndex === 0 ? 'South' : 'North';
    let number = 1;

    const hangerPositions = [
      ...Array.from({length: 21}, (_, index) => -114 + index * 11.4),
      ...Array.from({length: 4}, (_, index) => -174 + index * 12),
      ...Array.from({length: 4}, (_, index) => 138 + index * 12)
    ];

    for (const x of hangerPositions) {
      const topY = archYAt(x);
      const bottomY = ARCH.hangerBottomY;
      const length = topY - bottomY;
      addBox(
          hangers,
          `Hanger-${side}-${String(number).padStart(2, '0')}`,
          [0.18, length, 0.18],
          [x, (topY + bottomY) / 2, z],
          materials.hanger
      );
      number += 1;
    }
  }

  parent.add(hangers);
}

function createPiersAndApproaches(parent) {
  const group = new THREE.Group();
  group.name = 'Piers-And-Approaches';

  for (const [index, direction] of [['West', -1], ['East', 1]].entries()) {
    const side = index === 0 ? 'West' : 'East';
    addBox(group, `Main-Pier-${side}-Foundation`, [20, 3, 40], [direction * 132, -5.4, 0], materials.pier);
    addBox(group, `Main-Pier-${side}`, [5, 6, 32], [direction * 132, -1.8, 0], materials.pier);
    addBox(group, `Main-Pier-${side}-Cap`, [12, 1.5, 34], [direction * 132, 1.1, 0], materials.pier);
    addBox(group, `Side-Arch-End-Pier-${side}`, [8, 6, 30], [direction * 186, -2, 0], materials.pier);
    addBox(group, `Road-Approach-${side}`, [72, 0.9, 27], [direction * 222, 0.2, 0], materials.asphalt);

    for (const pierX of [205, 235]) {
      addBox(
          group,
          `Approach-Pier-${side}-${pierX}`,
          [3, 6, 24],
          [direction * pierX, -2.4, 0],
          materials.pier
      );
    }

    addBox(group, `Approach-Abutment-${side}`, [12, 5, 34], [direction * 254, -1.6, 0], materials.pier);
  }

  parent.add(group);
}

function createHankouChangfengBridge() {
  const bridge = new THREE.Group();
  bridge.name = 'Hankou-Changfeng-Bridge';

  createRiver(bridge);
  createDeck(bridge);
  createArchRibs(bridge);
  createHangers(bridge);
  createPiersAndApproaches(bridge);

  return bridge;
}

const exporter = new GLTFExporter();
const bridge = createHankouChangfengBridge();
const glb = await exporter.parseAsync(bridge, {binary: true});

await mkdir(path.dirname(output), {recursive: true});
await writeFile(output, Buffer.from(glb));
console.log(`${output}: ${glb.byteLength} bytes`);
