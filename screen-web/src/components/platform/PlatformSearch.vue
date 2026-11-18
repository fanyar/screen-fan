<template>
  <div
      ref="root"
      class="platform-search"
      :class="{expanded: mobileExpanded}"
      @keydown="handleKeydown"
  >
    <el-input
        v-model.trim="keyword"
        class="search-input"
        autocomplete="off"
        clearable
        placeholder="搜索作品、漫画、电影、动漫、模型、游戏、文章"
        @focus="openSearch"
        @input="handleInput"
        @clear="clearSearch"
    >
      <template #prefix>
        <el-icon><Search/></el-icon>
      </template>
    </el-input>

    <div v-if="visible" class="search-panel">
      <div v-if="loading" class="search-state">
        <el-icon class="is-loading"><Loading/></el-icon>
        <span>正在搜索</span>
      </div>

      <template v-else-if="hasResult">
        <div class="type-tabs">
          <button
              v-for="item in typeTabs"
              :key="item.value"
              type="button"
              :class="{active: activeType === item.value}"
              @click="changeType(item.value)"
          >
            {{ item.label }}
            <span>{{ item.count }}</span>
          </button>
        </div>
        <section
            v-for="group in resultGroups"
            :key="group.key"
            class="result-group"
        >
          <h3>{{ group.label }}<span>{{ group.items.length }}</span></h3>
          <button
              v-for="(item, index) in group.items"
              :key="item.id"
              type="button"
              class="result-card"
              :class="{active: activeIndex === group.start + index}"
              @click="openItem(group.key, item)"
          >
            <span class="result-cover">
              <img
                  v-if="item.cover"
                  :src="item.cover"
                  :alt="item.title"
                  loading="lazy"
              >
              <i v-else>{{ group.short }}</i>
            </span>
            <span class="result-info">
              <span class="result-title">
                <template v-for="(part, partIndex) in highlightText(item.title)" :key="partIndex">
                  <mark v-if="part.hit">{{ part.text }}</mark>
                  <template v-else>{{ part.text }}</template>
                </template>
              </span>
              <span class="result-desc">
                <template v-for="(part, partIndex) in highlightText(item.desc || item.note || '暂无描述')" :key="partIndex">
                  <mark v-if="part.hit">{{ part.text }}</mark>
                  <template v-else>{{ part.text }}</template>
                </template>
              </span>
              <span class="result-meta">{{ metaText(group.key, item) }}</span>
            </span>
          </button>
        </section>
        <footer class="search-total">共找到 {{ totalCount }} 条结果</footer>
      </template>

      <div v-else class="search-state">
        <el-icon><Search/></el-icon>
        <span>{{ keyword.length < 2 ? '输入至少 2 个字符' : '没有找到匹配内容' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getModels, globalSearch} from '@/api/platform'
import {PLATFORM_GAMES} from '@/const/games'

const router = useRouter()
const root = ref(null)
const keyword = ref('')
const loading = ref(false)
const visible = ref(false)
const mobileExpanded = ref(false)
const activeIndex = ref(0)
const activeType = ref('all')
const result = ref({
  works: [],
  comics: [],
  movies: [],
  anime: [],
  models: [],
  games: [],
  articles: [],
  total: 0
})

const emptySearchResult = () => ({
  works: [],
  comics: [],
  movies: [],
  anime: [],
  models: [],
  games: [],
  articles: [],
  total: 0
})
let timer = null
let requestId = 0

const groupDefinitions = [
  {key: 'works', label: '作品', short: '作品', path: 'works'},
  {key: 'comics', label: '漫画', short: '漫画', path: 'comics'},
  {key: 'movies', label: '电影', short: '电影', path: 'movies'},
  {key: 'anime', label: '动漫', short: '动漫', path: 'anime'},
  {key: 'models', label: '模型', short: '模型', path: 'models'},
  {key: 'games', label: '游戏', short: '游戏', path: 'games'},
  {key: 'articles', label: '文章', short: '文章', path: 'articles'}
]

const resultGroups = computed(() => {
  let start = 0
  return groupDefinitions
      .filter(definition => activeType.value === 'all' || activeType.value === definition.key)
      .map(definition => {
        const items = result.value[definition.key] || []
        const group = {...definition, items, start}
        start += items.length
        return group
      })
      .filter(group => group.items.length)
})
const flatItems = computed(() => resultGroups.value.flatMap(group => group.items.map(item => ({...item, type: group.key}))))
const hasResult = computed(() => flatItems.value.length > 0)
const totalCount = computed(() => Number(result.value.total || 0))
const typeTabs = computed(() => [
  {label: '全部', value: 'all', count: totalCount.value},
  ...groupDefinitions.map(item => ({
    label: item.label,
    value: item.key,
    count: (result.value[item.key] || []).length
  }))
])

const highlightText = (value) => {
  const text = String(value || '')
  const query = keyword.value.trim()
  if (!query) return [{text, hit: false}]
  const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text
      .split(new RegExp(`(${escaped})`, 'gi'))
      .filter(part => part !== '')
      .map(part => ({text: part, hit: part.toLowerCase() === query.toLowerCase()}))
}

const metaText = (type, item) => {
  if (type === 'works') return `${item.year} · ${(item.tags || []).slice(0, 2).join(' / ') || '作品'}`
  if (type === 'comics') return `${item.status} · ${item.chapters} 话`
  if (type === 'movies') return `${item.year} · ${item.genre} · ${item.rating} 分`
  if (type === 'anime') return `${item.year} · ${item.genre} · ${item.episodes} 集`
  if (type === 'models') return `${item.category} · ${(item.tags || []).slice(0, 2).join(' / ') || '模型'}`
  if (type === 'games') return `${(item.tags || []).slice(0, 2).join(' / ') || '游戏'} · ${item.badge}`
  return `${item.date} · ${item.viewCount} 次阅读`
}

const openSearch = () => {
  mobileExpanded.value = true
  if (keyword.value.length >= 2) visible.value = true
}

const clearSearch = () => {
  keyword.value = ''
  loading.value = false
  visible.value = false
  result.value = emptySearchResult()
}

const closePanel = () => {
  visible.value = false
  mobileExpanded.value = false
}

const changeType = (type) => {
  activeType.value = type
  activeIndex.value = 0
}

const handleInput = () => {
  clearTimeout(timer)
  if (keyword.value.length < 2) {
    loading.value = false
    visible.value = false
    return
  }

  timer = setTimeout(runSearch, 300)
}

const runSearch = async () => {
  const current = requestId + 1
  requestId = current
  loading.value = true
  visible.value = true
  activeIndex.value = 0
  activeType.value = 'all'
  try {
    const queryKeyword = keyword.value
    const [data, modelResult] = await Promise.all([
      globalSearch({keyword: queryKeyword, limit: 5}),
      getModels({keyword: queryKeyword, page: 1, pageSize: 5}).catch(() => ({items: []}))
    ])
    const normalizedKeyword = queryKeyword.toLowerCase()
    const games = PLATFORM_GAMES.filter(game => [game.title, game.desc, game.tags]
        .some(field => Array.isArray(field)
            ? field.some(tag => tag.toLowerCase().includes(normalizedKeyword))
            : field.toLowerCase().includes(normalizedKeyword)
        ))
        .slice(0, 5)
    if (requestId !== current) return
    result.value = {
      ...emptySearchResult(),
      ...data,
      models: modelResult.items || [],
      games,
      total: Number(data.total || 0) + (modelResult.items?.length || 0) + games.length
    }
  } catch (error) {
    if (requestId === current) {
      result.value = emptySearchResult()
    }
  } finally {
    if (requestId === current) loading.value = false
  }
}

const openItem = (type, item) => {
  const modelPath = item.fileUrl === '/platform/models/house-tour'
    ? '/platform/models/house-tour'
    : item.fileUrl === '/platform/models/villa'
      ? '/platform/models/villa'
      : item.fileUrl === '/platform/models/suzhou-garden'
        ? '/platform/models/suzhou-garden'
        : `/platform/models/${item.id}`
  const paths = {
    works: `/platform/works/${item.id}`,
    comics: `/platform/comics/${item.id}`,
    movies: `/platform/movies/${item.id}`,
    anime: `/platform/anime/${item.id}`,
    articles: `/platform/articles/${item.id}`,
    models: modelPath,
    games: item.path
  }
  closePanel()
  clearSearch()
  router.push(paths[type])
}

const moveActive = (direction) => {
  if (!flatItems.value.length) return
  activeIndex.value = (activeIndex.value + direction + flatItems.value.length) % flatItems.value.length
}

const handleKeydown = (event) => {
  if (!visible.value) return
  if (event.key === 'ArrowDown') {
    event.preventDefault()
    moveActive(1)
  }
  if (event.key === 'ArrowUp') {
    event.preventDefault()
    moveActive(-1)
  }
  if (event.key === 'Enter' && flatItems.value[activeIndex.value]) {
    event.preventDefault()
    const item = flatItems.value[activeIndex.value]
    openItem(item.type, item)
  }
  if (event.key === 'Escape') {
    closePanel()
  }
}

const handleOutside = (event) => {
  if (!root.value?.contains(event.target)) closePanel()
}

onMounted(() => {
  document.addEventListener('pointerdown', handleOutside)
})

onBeforeUnmount(() => {
  clearTimeout(timer)
  document.removeEventListener('pointerdown', handleOutside)
})
</script>

<style scoped lang="scss">
.platform-search {
  position: relative;
  width: 220px;
  transition: width .2s ease;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  height: 34px;
  border-radius: 4px;
  background: var(--sx-canvas);
  box-shadow: 0 0 0 1px var(--sx-line) inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: var(--sx-surface);
  box-shadow: 0 0 0 1px var(--sx-primary) inset;
}

.search-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 50;
  width: min(440px, calc(100vw - 28px));
  max-height: min(70vh, 640px);
  overflow: auto;
  padding: 14px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
  box-shadow: var(--sx-card-shadow-hover);
}

.search-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 88px;
  color: var(--sx-muted);
  font-size: 14px;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding-bottom: 10px;
  margin-bottom: 10px;
  border-bottom: 1px solid var(--sx-line);

  button {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    height: 28px;
    padding: 0 9px;
    border: 1px solid var(--sx-line);
    border-radius: 4px;
    background: var(--sx-surface);
    color: var(--sx-muted);
    font-size: 13px;
    cursor: pointer;

    span {
      font-size: 12px;
    }

    &.active {
      border-color: var(--sx-primary);
      background: var(--sx-primary-soft);
      color: var(--sx-primary);
    }
  }
}

.result-group {
  & + .result-group {
    margin-top: 16px;
  }

  h3 {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0 0 8px;
    color: var(--sx-title);
    font-size: 14px;
    line-height: 22px;

    span {
      color: var(--sx-muted);
      font-size: 12px;
      font-weight: 400;
    }
  }
}

.result-card {
  display: grid;
  align-items: center;
  width: 100%;
  gap: 10px;
  grid-template-columns: 52px minmax(0, 1fr);
  padding: 8px;
  border: 1px solid var(--sx-line);
  border-radius: 5px;
  background: var(--sx-surface);
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color .2s ease, background-color .2s ease;

  & + .result-card {
    margin-top: 7px;
  }

  &:hover,
  &.active {
    border-color: var(--sx-primary);
    background: var(--sx-primary-soft);
  }
}

.result-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  width: 52px;
  height: 52px;
  border-radius: 4px;
  background: var(--sx-cover-bg);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  i {
    color: var(--sx-muted);
    font-size: 12px;
    font-style: normal;
  }
}

.result-info {
  display: block;
  min-width: 0;
}

.result-title,
.result-desc,
.result-meta {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-title {
  color: var(--sx-title);
  font-size: 15px;
  font-weight: 500;
  line-height: 22px;
}

.result-title mark,
.result-desc mark {
  padding: 0 1px;
  border-radius: 2px;
  background: rgba(250, 204, 21, .48);
  color: inherit;
}

.result-desc {
  margin-top: 2px;
  color: var(--sx-body);
  font-size: 13px;
  line-height: 20px;
}

.result-meta {
  margin-top: 3px;
  color: var(--sx-muted);
  font-size: 12px;
  line-height: 18px;
}

.search-total {
  margin-top: 12px;
  color: var(--sx-muted);
  font-size: 13px;
  text-align: center;
}

@media (max-width: 720px) {
  .platform-search {
    width: 34px;

    &.expanded {
      width: min(220px, 42vw);
    }
  }
}
</style>
