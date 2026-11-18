import {mkdir, readFile, rm, writeFile} from 'node:fs/promises'
import {dirname, join} from 'node:path'
import {fileURLToPath} from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const outputDir = join(__dirname, '../public/mock')
const publicDir = join(__dirname, '../public')
const backendBase = (process.env.BACKEND_BASE || 'http://localhost:8666').replace(/\/$/, '')
const localAssetPattern = /\/(?:uploads|platform)\/[^\s"'()\\<>[\]]+/g
const assetExtensionPattern = /\.(png|jpe?g|gif|svg|webp|avif|glb|gltf|mp4|webm|mp3|json|pdf)$/i
const devAssetPattern = /https?:\/\/(?:localhost|127\.0\.0\.1):3000\/src\/assets\/[^\s"'()\\<>[\]]+/g
const assets = new Map()

async function getJson(path) {
  const response = await fetch(new URL(path, backendBase))
  if (!response.ok) {
    throw new Error(`GET ${path} failed: HTTP ${response.status}`)
  }
  return response.json()
}

async function fetchPage(path) {
  const first = await getJson(`${path}?page=1&pageSize=50`)
  const items = [...first.items]
  const total = Number(first.total || items.length)
  const pageCount = Math.ceil(total / 50)

  for (let page = 2; page <= pageCount; page += 1) {
    const result = await getJson(`${path}?page=${page}&pageSize=50`)
    items.push(...result.items)
  }

  if (items.length !== total) {
    throw new Error(`${path}: expected ${total} items, received ${items.length}`)
  }

  return {
    items,
    total,
    page: first.page || 1,
    pageSize: 50
  }
}

function collectAssetPaths(value, paths = new Set()) {
  if (typeof value === 'string') {
    for (const match of value.matchAll(localAssetPattern) || []) {
      const source = match[0]
      const previousCharacter = match.index > 0 ? value[match.index - 1] : ''
      if (previousCharacter && /[\w:/.-]/.test(previousCharacter)) continue
      paths.add(source.replace(/[.,;!?]+$/, ''))
    }
    for (const match of value.match(devAssetPattern) || []) {
      paths.add(match.replace(/[.,;!?]+$/, ''))
    }
  } else if (Array.isArray(value)) {
    value.forEach(item => collectAssetPaths(item, paths))
  } else if (value && typeof value === 'object') {
    Object.values(value).forEach(item => collectAssetPaths(item, paths))
  }
  return paths
}

async function downloadAssets(value) {
  for (const sourcePath of collectAssetPaths(value)) {
    if (!assetExtensionPattern.test(sourcePath)) continue
    if (assets.has(sourcePath)) continue

    const sourceUrl = sourcePath.startsWith('http')
        ? new URL(sourcePath)
        : new URL(sourcePath, backendBase)
    const normalizedPath = sourceUrl.pathname
    const targetPath = `mock/assets${normalizedPath}`
    const targetFile = join(outputDir, targetPath.slice('mock/'.length))
    let content
    try {
      const response = await fetch(new URL(normalizedPath, backendBase))
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      content = Buffer.from(await response.arrayBuffer())
    } catch (error) {
      try {
        content = normalizedPath.startsWith('/src/assets/')
            ? await readFile(join(__dirname, '..', normalizedPath))
            : await readFile(join(publicDir, normalizedPath))
      } catch (fallbackError) {
        const detail = `${sourcePath} failed from backend (${error.message}) and local assets (${fallbackError.message})`
        throw new Error(detail)
      }
    }

    await mkdir(dirname(targetFile), {recursive: true})
    await writeFile(targetFile, content)
    assets.set(sourcePath, targetPath)
  }
}

function rewriteAssets(value) {
  if (typeof value === 'string') {
    return [...assets.entries()].reduce(
        (text, [sourcePath, targetPath]) => text.replaceAll(sourcePath, targetPath),
        value
    )
  }
  if (Array.isArray(value)) {
    return value.map(rewriteAssets)
  }
  if (value && typeof value === 'object') {
    return Object.fromEntries(
        Object.entries(value).map(([key, item]) => [key, rewriteAssets(item)])
    )
  }
  return value
}

async function writeJson(name, value) {
  const targetFile = join(outputDir, name)
  await mkdir(dirname(targetFile), {recursive: true})
  await writeFile(targetFile, `${JSON.stringify(value, null, 2)}\n`, 'utf8')
}

async function exportContent(kind) {
  const list = await fetchPage(`/api/${kind}`)
  const details = {}
  for (const item of list.items) {
    details[item.id] = await getJson(`/api/${kind}/${item.id}`)
  }
  return {list, details}
}

async function exportModels() {
  const list = await fetchPage('/api/models')
  const details = {}
  const progress = {}
  const snapshots = {}

  for (const item of list.items) {
    details[item.id] = await getJson(`/api/models/${item.id}`)
    progress[item.id] = await getJson(`/api/models/${item.id}/progress-tree`)
    snapshots[item.id] = await getJson(`/api/models/${item.id}/progress/snapshots`)
  }

  return {list, details, progress, snapshots}
}

rm(outputDir, {recursive: true, force: true}).then(async () => {
  await mkdir(outputDir, {recursive: true})

  const contentKinds = ['works', 'comics', 'movies', 'anime', 'articles']
  const content = Object.fromEntries(
      await Promise.all(contentKinds.map(async kind => [kind, await exportContent(kind)]))
  )
  const models = await exportModels()
  const homeConfig = await getJson('/api/home-config')
  const contentFacets = await getJson('/api/content-facets')
  const modelFacets = await getJson('/api/models/facets')
  const dataset = {
    version: 1,
    generatedAt: new Date().toISOString(),
    works: content.works,
    comics: content.comics,
    movies: content.movies,
    anime: content.anime,
    articles: content.articles,
    models,
    homeConfig,
    contentFacets,
    modelFacets
  }

  await downloadAssets(dataset)
  const portableDataset = rewriteAssets(dataset)
  await writeJson('data.json', portableDataset)
  await writeJson('manifest.json', {
    version: portableDataset.version,
    generatedAt: portableDataset.generatedAt,
    counts: {
      works: portableDataset.works.list.total,
      comics: portableDataset.comics.list.total,
      movies: portableDataset.movies.list.total,
      anime: portableDataset.anime.list.total,
      articles: portableDataset.articles.list.total,
      models: portableDataset.models.list.total
    },
    assets: assets.size,
    source: backendBase
  })

  console.log(`Mock data exported: ${assets.size} assets`)
}).catch(error => {
  console.error(error)
  process.exitCode = 1
})
