<template>
  <div class="platform-page platform-page--detail comic-reader-page">
    <div v-loading="loading" class="reader-shell">
      <template v-if="comic && chapter">
        <header class="reader-toolbar">
          <el-button @click="returnToDetail">
            <el-icon><ArrowLeft/></el-icon>
            <span>详情</span>
          </el-button>
          <div class="reader-heading">
            <span class="reader-comic">{{ comic.title }}</span>
            <h1 class="reader-title">{{ chapter.chapterNo }}. {{ chapter.title }}</h1>
          </div>
          <el-select
              v-model="selectedChapterId"
              class="reader-chapter-select"
              @change="openChapter"
          >
            <el-option
                v-for="item in chapters"
                :key="item.id"
                :label="`${item.chapterNo}. ${item.title}`"
                :value="item.id"
            />
          </el-select>
        </header>

        <div class="reader-mode">
          <div class="mode-switch" role="tablist" aria-label="阅读模式">
            <button
                v-for="item in readingModes"
                :key="item.value"
                type="button"
                role="tab"
                :aria-selected="readingMode === item.value"
                :class="{active: readingMode === item.value}"
                @click="readingMode = item.value"
            >
              {{ item.label }}
            </button>
          </div>
          <span v-if="readingMode === 'single'" class="page-indicator">
            {{ pageIndex + 1 }} / {{ pages.length || 1 }}
          </span>
        </div>

        <div v-if="pages.length" class="reader-stage scrollBar" :class="{'reader-stage--single': readingMode === 'single'}">
          <template v-if="readingMode === 'continuous'">
            <img
                v-for="(page, index) in pages"
                :key="page"
                :src="page"
                :alt="`${chapter.title} 第 ${index + 1} 页`"
                loading="lazy"
            >
          </template>
          <img
              v-else
              :key="pages[pageIndex]"
              :src="pages[pageIndex]"
              :alt="`${chapter.title} 第 ${pageIndex + 1} 页`"
          >
        </div>
        <el-empty v-else description="本话页面待上传" :image-size="110"/>

        <footer class="reader-nav">
          <el-button :disabled="!previousChapter" @click="openChapter(previousChapter?.id)">
            <el-icon><ArrowLeft/></el-icon>
            <span>上一话</span>
          </el-button>
          <el-button
              v-if="readingMode === 'single' && pages.length"
              :disabled="pageIndex === 0"
              @click="turnPage(-1)"
          >
            上一页
          </el-button>
          <el-button
              v-if="readingMode === 'single' && pages.length"
              :disabled="pageIndex >= pages.length - 1"
              @click="turnPage(1)"
          >
            下一页
          </el-button>
          <el-button :disabled="!nextChapter" @click="openChapter(nextChapter?.id)">
            <span>下一话</span>
            <el-icon><ArrowRight/></el-icon>
          </el-button>
        </footer>
      </template>
    </div>

    <el-empty v-if="!loading && !comic" description="章节不存在"/>
    <el-alert
        v-if="loadFailed"
        title="漫画章节加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getComic, saveReadingProgress} from '@/api/platform'
import {useHistoryStore} from '@/store/modules/history'
import {preservedListRouteQuery} from '@/util/list-query'

const route = useRoute()
const router = useRouter()
const comic = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const readingMode = ref('continuous')
const pageIndex = ref(0)
const selectedChapterId = ref(String(route.params.chapterId || ''))
const readingModes = [
  {label: '连续', value: 'continuous'},
  {label: '单页', value: 'single'}
]
const historyStore = useHistoryStore()

const returnToDetail = () => router.push({
  path: `/platform/comics/${route.params.comicId}`,
  query: preservedListRouteQuery(route.query)
})

const chapters = computed(() => {
  return [...(comic.value?.chapterList || [])].sort((left, right) => left.chapterNo - right.chapterNo)
})
const chapter = computed(() => chapters.value.find(item => item.id === selectedChapterId.value))
const pages = computed(() => chapter.value?.pages || [])
const chapterIndex = computed(() => chapters.value.findIndex(item => item.id === selectedChapterId.value))
const previousChapter = computed(() => chapterIndex.value > 0 ? chapters.value[chapterIndex.value - 1] : null)
const nextChapter = computed(() => (
  chapterIndex.value >= 0 && chapterIndex.value < chapters.value.length - 1
    ? chapters.value[chapterIndex.value + 1]
    : null
))

const recordChapter = () => {
  if (!comic.value || !chapter.value) return
  historyStore.record(
      'comics',
      comic.value,
      `/platform/comics/${comic.value.id}/chapters/${chapter.value.id}`,
      `${chapter.value.chapterNo}. ${chapter.value.title}`
  )
  saveReadingProgress(comic.value.id, {
    chapterId: chapter.value.id,
    chapterNo: chapter.value.chapterNo
  }).catch(() => {})
}

const loadReader = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    comic.value = await getComic(route.params.comicId)
    if (!chapters.value.some(item => item.id === selectedChapterId.value)) {
      selectedChapterId.value = chapters.value[0]?.id || ''
    }
  } catch (error) {
    comic.value = null
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const openChapter = (chapterId) => {
  if (!chapterId) return
  router.push({
    path: `/platform/comics/${route.params.comicId}/chapters/${chapterId}`,
    query: preservedListRouteQuery(route.query)
  })
}

const turnPage = (direction) => {
  const next = pageIndex.value + direction
  if (next < 0 || next >= pages.value.length) return
  pageIndex.value = next
}

const handleKeydown = (event) => {
  if (event.target.closest('input, textarea, select')) return
  if (event.key === 'ArrowLeft') {
    if (readingMode.value === 'single') turnPage(-1)
    else openChapter(previousChapter.value?.id)
  }
  if (event.key === 'ArrowRight') {
    if (readingMode.value === 'single') turnPage(1)
    else openChapter(nextChapter.value?.id)
  }
}

watch(selectedChapterId, () => {
  pageIndex.value = 0
})
watch(chapter, (value) => {
  if (value) recordChapter()
})

watch(() => route.params.chapterId, (value) => {
  if (value && value !== selectedChapterId.value) {
    selectedChapterId.value = String(value)
  }
})

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  loadReader()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.comic-reader-page {
  padding: 24px 20px 40px;
}

.reader-shell {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  width: 100%;
}

.reader-toolbar {
  display: grid;
  flex: none;
  align-items: center;
  grid-template-columns: auto minmax(0, 1fr) 220px;
  gap: 18px;
  margin-bottom: 16px;
}

.reader-heading {
  min-width: 0;
}

.reader-comic,
.page-indicator {
  color: var(--sx-muted);
  font-size: 14px;
}

.reader-title {
  overflow: hidden;
  margin: 3px 0 0;
  color: var(--sx-title);
  font-size: 22px;
  line-height: 30px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reader-mode {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.mode-switch {
  display: flex;
  padding: 3px;
  border: 1px solid var(--sx-line);
  border-radius: 5px;
  background: var(--sx-surface);

  button {
    width: 62px;
    height: 30px;
    border: 0;
    border-radius: 4px;
    background: transparent;
    color: var(--sx-muted);
    font-size: 14px;
    cursor: pointer;
  }

  .active {
    background: var(--sx-primary-soft);
    color: var(--sx-primary);
    font-weight: 500;
  }
}

.reader-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-height: 0;
  gap: 10px;
  overflow-y: auto;
  padding: 14px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: #0d131d;

  img {
    display: block;
    width: 100%;
    border-radius: 4px;
    object-fit: contain;
    background: var(--sx-surface);
  }
}

.reader-stage--single {
  justify-content: center;
}

.reader-stage--single img {
  max-height: 100%;
  width: auto;
  max-width: 100%;
}

.reader-nav {
  display: flex;
  flex: none;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 10px;
  margin-top: 18px;

  .el-button + .el-button {
    margin-left: 0;
  }
}

@media (max-width: 720px) {
  .reader-toolbar {
    grid-template-columns: 1fr;
  }

  .reader-chapter-select {
    width: 100%;
  }
}
</style>
