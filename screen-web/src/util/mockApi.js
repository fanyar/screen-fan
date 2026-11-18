let datasetPromise = null

async function loadDataset() {
  if (!datasetPromise) {
    datasetPromise = fetch(`${import.meta.env.BASE_URL}mock/data.json`).then(async response => {
      if (!response.ok) {
        throw new Error(`展示数据加载失败（HTTP ${response.status}）`)
      }
      return response.json()
    })
  }
  return datasetPromise
}

function compactParams(params = {}) {
  return Object.fromEntries(
      Object.entries(params).filter(([, value]) => value !== '' && value !== null && value !== undefined)
  )
}

function matchesKeyword(item, keyword, fields) {
  const query = String(keyword || '').trim().toLowerCase()
  if (!query) return true
  return fields.some(field => {
    const value = field(item)
    if (Array.isArray(value)) return value.some(part => String(part).toLowerCase().includes(query))
    return String(value || '').toLowerCase().includes(query)
  })
}

function paginate(items, params = {}, defaultPageSize = 12) {
  const page = Math.max(1, Number(params.page) || 1)
  const pageSize = Math.min(50, Math.max(1, Number(params.pageSize) || defaultPageSize))
  const start = (page - 1) * pageSize
  return {
    items: items.slice(start, start + pageSize),
    total: items.length,
    page,
    pageSize
  }
}

function contentMatcher(kind, params = {}) {
  return item => {
    if (!matchesKeyword(item, params.keyword, [item => item.title, item => item.desc, item => item.note, item => item.tags])) {
      return false
    }
    if (params.tag && !(item.tags || []).includes(params.tag)) return false
    if (params.status && item.status !== params.status) return false
    if (params.genre && item.genre !== params.genre) return false
    if (params.year && Number(item.year) !== Number(params.year)) return false
    return kind !== 'articles' || item.status === 'published'
  }
}

export async function getMockList(kind, params = {}) {
  const dataset = await loadDataset()
  const source = dataset[kind]
  if (!source?.list) throw new Error(`未知内容类型：${kind}`)
  const filtered = source.list.items.filter(contentMatcher(kind, params))
  return paginate(filtered, params, kind === 'models' ? 9 : 12)
}

export async function getMockDetail(kind, id) {
  const dataset = await loadDataset()
  const detail = dataset[kind]?.details?.[id]
  if (!detail) throw new Error('内容不存在')
  return detail
}

export async function getMockHomeConfig() {
  const dataset = await loadDataset()
  return dataset.homeConfig
}

export async function getContentFacets() {
  const dataset = await loadDataset()
  return dataset.contentFacets
}

export async function getModelFacets() {
  const dataset = await loadDataset()
  return dataset.modelFacets
}

export async function getModelProgress(id) {
  const dataset = await loadDataset()
  const tree = dataset.models.progress[id]
  if (!tree) throw new Error('模型进度不存在')
  return tree
}

export async function getModelProgressSnapshots(id) {
  const dataset = await loadDataset()
  return dataset.models.snapshots[id] || []
}

export async function globalSearch(params = {}) {
  const dataset = await loadDataset()
  const query = compactParams(params)
  const groups = ['works', 'comics', 'movies', 'anime', 'articles']
  const result = Object.fromEntries(groups.map(kind => [
    kind,
    dataset[kind].list.items.filter(contentMatcher(kind, query)).slice(0, Number(query.limit) || 5)
  ]))
  result.total = groups.reduce((total, kind) => total + result[kind].length, 0)
  return result
}

export async function submitContactMessage() {
  throw new Error('演示环境暂不支持提交联系信息')
}

export async function getComments() {
  return []
}

export async function createComment() {
  throw new Error('演示环境暂不支持评论')
}

export async function getReadingProgress(comicId) {
  const raw = localStorage.getItem(`comic-progress:${comicId}`)
  if (!raw) return undefined
  try {
    return JSON.parse(raw)
  } catch (error) {
    return undefined
  }
}

export async function saveReadingProgress(comicId, data) {
  const progress = {
    comicId,
    chapterId: data.chapterId,
    chapterNo: data.chapterNo,
    updatedAt: new Date().toISOString()
  }
  localStorage.setItem(`comic-progress:${comicId}`, JSON.stringify(progress))
  return progress
}
