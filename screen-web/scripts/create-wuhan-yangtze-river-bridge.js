import {mkdir, writeFile} from 'node:fs/promises';
import path from 'node:path';
import * as THREE from 'three';
import {GLTFExporter} from 'three/examples/jsm/exporters/GLTFExporter.js';

const output = process.argv[2] || 'public/platform/models/wuhan-yangtze-river-bridge.glb';

globalThis.FileReader = class FileReader {
  readAsArrayBuffer(blob) {
    blob.arrayBuffer().then(buffer => {
      this.result = buffer;
      this.onloadend?.();
    });
  }
};

const boxGeometry = new THREE.BoxGeometry(1, 1, 1);
const spanBoundaries = [-51, -39, -27, -15, -3, 9, 21, 33, 45, 51];
const materials = {
  water: new THREE.MeshStandardMaterial({
    color: 0x1d4f8f,
    roughness: 0.16,
    metalness: 0.1,
    transparent: true,
    opacity: 0.9
  }),
  pier: new THREE.MeshStandardMaterial({color: 0x8d959f, roughness: 0.82, metalness: 0.03}),
  concrete: new THREE.MeshStandardMaterial({color: 0xc9c4b8, roughness: 0.78, metalness: 0.02}),
  steel: new THREE.MeshStandardMaterial({color: 0x2f6b4f, roughness: 0.48, metalness: 0.42}),
  road: new THREE.MeshStandardMaterial({color: 0x30343a, roughness: 0.85, metalness: 0.02}),
  walkway: new THREE.MeshStandardMaterial({color: 0xb8b2a5, roughness: 0.75, metalness: 0.02}),
  rail: new THREE.MeshStandardMaterial({color: 0x666f78, roughness: 0.35, metalness: 0.72})
};

function addBox(parent, name, size, position, material, progressKey = null, rotation = [0, 0, 0]) {
  const mesh = new THREE.Mesh(boxGeometry, material);
  mesh.name = name;
  mesh.scale.set(size[0], size[1], size[2]);
  mesh.position.set(position[0], position[1], position[2]);
  mesh.rotation.set(rotation[0], rotation[1], rotation[2]);
  if (progressKey) {
    mesh.userData.progressKey = progressKey;
  }
  parent.add(mesh);
  return mesh;
}

function createRiver(parent) {
  const river = new THREE.Group();
  river.name = 'Yangtze-River';
  addBox(river, 'River-Surface', [190, 0.2, 58], [0, -3.2, 0], materials.water);
  parent.add(river);
}

function createPiers(parent) {
  const piers = new THREE.Group();
  piers.name = 'Bridge-Piers';
  for (const [index, x] of spanBoundaries.slice(1, -1).entries()) {
    const number = index + 1;
    addBox(piers, `Pier-${String(number).padStart(2, '0')}-Foundation`, [3.8, 1, 10], [x, -6.5, 0], materials.pier);
    addBox(piers, `Pier-${String(number).padStart(2, '0')}`, [2.4, 8.5, 7], [x, -1.9, 0], materials.pier);
    addBox(piers, `Pier-${String(number).padStart(2, '0')}-Cap`, [4, 1, 9], [x, 2.8, 0], materials.concrete);
  }
  parent.add(piers);
}

function createSegmentMember(parent, spanNo, x1, x2, memberKey, memberType) {
  const length = x2 - x1;
  const centerX = (x1 + x2) / 2;
  const group = new THREE.Group();
  group.name = `Unit-01.Span-${String(spanNo).padStart(2, '0')}.${memberType}`;
  const key = `Unit-01.Span-${String(spanNo).padStart(2, '0')}.${memberKey}`;

  if (memberType === 'girder') {
    const bottomY = 3.6;
    const topY = 6.8;
    for (const [sideIndex, z] of [-2.7, 2.7].entries()) {
      const sideName = sideIndex === 0 ? 'South' : 'North';
      addBox(group, `Span-${spanNo}-${sideName}-Bottom-Chord`, [length, 0.26, 0.26], [centerX, bottomY, z], materials.steel, key);
      addBox(group, `Span-${spanNo}-${sideName}-Top-Chord`, [length, 0.26, 0.26], [centerX, topY, z], materials.steel, key);
      addBox(group, `Span-${spanNo}-${sideName}-Vertical-Start`, [0.18, topY - bottomY, 0.18], [x1, (bottomY + topY) / 2, z], materials.steel, key);
      addBox(group, `Span-${spanNo}-${sideName}-Vertical-End`, [0.18, topY - bottomY, 0.18], [x2, (bottomY + topY) / 2, z], materials.steel, key);
      const dy = topY - bottomY;
      const braceLength = Math.sqrt(length * length + dy * dy);
      addBox(
        group,
        `Span-${spanNo}-${sideName}-Diagonal`,
        [braceLength, 0.15, 0.15],
        [centerX, (bottomY + topY) / 2, z],
        materials.steel,
        key,
        [0, 0, Math.atan2(dy, length)]
      );
      addBox(
        group,
        `Span-${spanNo}-${sideName}-Truss-Panel`,
        [length, topY - bottomY, 0.12],
        [centerX, (bottomY + topY) / 2, z + (sideIndex === 0 ? 0.06 : -0.06)],
        materials.steel,
        key
      );
    }
  }

  if (memberType === 'deck') {
    addBox(group, `Span-${spanNo}-Road-Deck`, [length, 0.6, 8], [centerX, 6.55, 0], materials.road, key);
    for (const [index, side] of [-4, 4].entries()) {
      const sideName = index === 0 ? 'South' : 'North';
      addBox(group, `Span-${spanNo}-${sideName}-Walkway`, [length, 0.32, 0.8], [centerX, 6.78, side], materials.walkway, key);
    }
    for (const x of [x1 + 3, centerX, x2 - 3]) {
      addBox(group, `Span-${spanNo}-Upper-Cross-Beam-${x}`, [0.24, 0.28, 8], [x, 6.7, 0], materials.steel, key);
    }
  }

  if (memberType === 'lower-beam') {
    addBox(group, `Span-${spanNo}-Railway-Deck`, [length, 0.45, 5.4], [centerX, 3.4, 0], materials.concrete, key);
    for (const [index, side] of [-0.8, 0.8].entries()) {
      const sideName = index === 0 ? 'West' : 'East';
      addBox(group, `Span-${spanNo}-${sideName}-Rail`, [length, 0.1, 0.16], [centerX, 3.75, side], materials.rail, key);
    }
    for (const x of [x1 + 3, centerX, x2 - 3]) {
      addBox(group, `Span-${spanNo}-Lower-Cross-Beam-${x}`, [0.24, 0.28, 5.4], [x, 3.65, 0], materials.steel, key);
    }
  }

  parent.add(group);
}

function createSegmentedBridge(parent) {
  const bridge = new THREE.Group();
  bridge.name = 'Segmented-Bridge-System';
  for (let index = 0; index < spanBoundaries.length - 1; index += 1) {
    const spanNo = index + 1;
    const x1 = spanBoundaries[index];
    const x2 = spanBoundaries[index + 1];
    createSegmentMember(bridge, spanNo, x1, x2, 'girder', 'girder');
    createSegmentMember(bridge, spanNo, x1, x2, 'deck', 'deck');
    createSegmentMember(bridge, spanNo, x1, x2, 'lower-beam', 'lower-beam');
  }
  parent.add(bridge);
}

function createBridgeheads(parent) {
  const bridgeheads = new THREE.Group();
  bridgeheads.name = 'Bridgehead-Towers';
  for (const [index, x] of [-56, 56].entries()) {
    const side = index === 0 ? 'West' : 'East';
    addBox(bridgeheads, `Bridgehead-${side}-Base`, [6, 3.5, 11], [x, 1.5, 0], materials.concrete);
    addBox(bridgeheads, `Bridgehead-${side}-Tower`, [4.5, 8, 9], [x, 7.5, 0], materials.concrete);
    addBox(bridgeheads, `Bridgehead-${side}-Roof`, [5.5, 1.2, 10], [x, 12.1, 0], materials.concrete);
  }
  parent.add(bridgeheads);
}

function createApproaches(parent) {
  const approaches = new THREE.Group();
  approaches.name = 'Approach-Bridges';
  for (const [side, direction] of [['West', -1], ['East', 1]]) {
    const x = direction * 66;
    addBox(approaches, `Road-Approach-${side}`, [24, 0.5, 8], [x, 6.3, 0], materials.road);
    addBox(approaches, `Rail-Approach-${side}`, [24, 0.45, 5.4], [x, 3.4, 0], materials.concrete);
    addBox(approaches, `Approach-Pier-${side}-1`, [1.7, 6, 5], [direction * 63, 0.2, 0], materials.pier);
    addBox(approaches, `Approach-Pier-${side}-2`, [1.7, 6, 5], [direction * 73, 0.2, 0], materials.pier);
  }
  parent.add(approaches);
}

function createWuhanYangtzeRiverBridge() {
  const bridge = new THREE.Group();
  bridge.name = 'Wuhan-Yangtze-River-Bridge';
  createRiver(bridge);
  createPiers(bridge);
  createSegmentedBridge(bridge);
  createBridgeheads(bridge);
  createApproaches(bridge);
  return bridge;
}

const exporter = new GLTFExporter();
const bridge = createWuhanYangtzeRiverBridge();
const glb = await exporter.parseAsync(bridge, {binary: true});

await mkdir(path.dirname(output), {recursive: true});
await writeFile(output, Buffer.from(glb));
console.log(`${output}: ${glb.byteLength} bytes, 27 progress members`);
