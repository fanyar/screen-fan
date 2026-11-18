import {computed, ref} from 'vue'
import {defineStore} from 'pinia'
import {getStore, setStore} from '@/util/store'
import {
  PLATFORM_FAVORITE_TYPE_LABELS,
  PLATFORM_HISTORY_STORAGE_KEY,
  PLATFORM_FAVORITE_TYPES
} from '@/const/platform'

const MAX_HISTORY = 100

const createSnapshot = (type, item) => {
  const base = {
    id: String(item?.id || ''),
    type,
    title: item?.title || '未命名内容',
    cover: item?.cover || '',
    desc: item?.desc || item?.note || ''
  }

  if (type === PLATFORM_FAVORITE_TYPES.WORKS) {
    return {...base, meta: `${item.year || ''} · ${(item.tags || []).slice(0, 2).join(' / ')}`.replace(/^ · /, '')}
  }
  if (type === PLATFORM_FAVORITE_TYPES.COMICS) {
    return {...base, meta: `${item.status || ''} · ${item.chapters || 0} 话`}
  }
  if (type === PLATFORM_FAVORITE_TYPES.MOVIES) {
    return {...base, meta: `${item.year || ''} · ${item.genre || ''} · ${item.rating || 0} 分`}
  }
  if (type === PLATFORM_FAVORITE_TYPES.ANIME) {
    return {...base, meta: `${item.year || ''} · ${item.genre || ''} · ${item.episodes || 0} 集`}
  }
  if (type === PLATFORM_FAVORITE_TYPES.ARTICLES) {
    return {...base, meta: `${item.date || ''} · ${item.viewCount || 0} 次阅读`}
  }
  return base
}

export const useHistoryStore = defineStore('history', () => {
  const items = ref(getStore({name: PLATFORM_HISTORY_STORAGE_KEY}) || [])
  const history = computed(() => items.value)
  const count = computed(() => items.value.length)

  const persist = () => {
    setStore({name: PLATFORM_HISTORY_STORAGE_KEY, content: items.value})
  }

  const find = (type, id) => {
    return items.value.find(item => item.type === type && item.id === String(id))
  }

  const record = (type, item, path, subtitle = '', options = {}) => {
    const id = String(item?.id || '')
    if (!id || !path || !PLATFORM_FAVORITE_TYPE_LABELS[type]) return

    const existingIndex = items.value.findIndex(row => row.type === type && row.id === id)
    const existing = existingIndex >= 0 ? items.value[existingIndex] : null
    const next = {
      ...(existing || createSnapshot(type, item)),
      ...createSnapshot(type, item),
      route: options.preserveRoute && existing?.route ? existing.route : path,
      subtitle: options.preserveSubtitle && existing?.subtitle ? existing.subtitle : subtitle,
      visitedAt: Date.now()
    }

    if (existingIndex >= 0) {
      items.value.splice(existingIndex, 1)
    }
    items.value.unshift(next)
    if (items.value.length > MAX_HISTORY) {
      items.value.length = MAX_HISTORY
    }
    persist()
  }

  const remove = (type, id) => {
    const existingIndex = items.value.findIndex(row => row.type === type && row.id === String(id))
    if (existingIndex < 0) return
    items.value.splice(existingIndex, 1)
    persist()
  }

  const clear = () => {
    items.value = []
    persist()
  }

  return {
    items,
    history,
    count,
    find,
    record,
    remove,
    clear
  }
})
