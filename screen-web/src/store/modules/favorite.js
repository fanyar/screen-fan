import {computed, ref} from 'vue'
import {defineStore} from 'pinia'
import {getStore, setStore} from '@/util/store'
import {
  PLATFORM_FAVORITE_STORAGE_KEY,
  PLATFORM_FAVORITE_TYPE_LABELS,
  PLATFORM_FAVORITE_TYPES
} from '@/const/platform'

const MAX_FAVORITES = 200

const createSnapshot = (type, item) => {
  const base = {
    id: String(item?.id || ''),
    type,
    title: item?.title || '未命名内容',
    cover: item?.cover || '',
    desc: item?.desc || item?.note || '',
    savedAt: Date.now()
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

export const useFavoriteStore = defineStore('favorite', () => {
  const items = ref(getStore({name: PLATFORM_FAVORITE_STORAGE_KEY}) || [])
  const favorites = computed(() => items.value)
  const count = computed(() => items.value.length)

  const persist = () => {
    setStore({name: PLATFORM_FAVORITE_STORAGE_KEY, content: items.value})
  }

  const isFavorite = (type, id) => {
    return items.value.some(item => item.type === type && item.id === String(id))
  }

  const toggle = (type, item) => {
    const id = String(item?.id || '')
    if (!id || !PLATFORM_FAVORITE_TYPE_LABELS[type]) return false

    const existingIndex = items.value.findIndex(row => row.type === type && row.id === id)
    if (existingIndex >= 0) {
      items.value.splice(existingIndex, 1)
      persist()
      return 'removed'
    }

    if (items.value.length >= MAX_FAVORITES) return false
    items.value.unshift(createSnapshot(type, item))
    persist()
    return 'added'
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
    favorites,
    count,
    isFavorite,
    toggle,
    remove,
    clear
  }
})
