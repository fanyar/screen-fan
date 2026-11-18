<template>
  <div class="platform-page platform-page--detail">
    <div v-loading="loading" class="detail-shell">
      <PlatformDetailLayout
          class="comic-detail-panel"
          v-if="comic"
          :title="comic.title"
          :cover="comic.cover"
          :description="comic.desc"
          :tags="comicTags"
          :meta="comicMeta"
          aspect="poster"
          @back="router.push('/platform/comics')"
      >
        <template #title-actions>
          <PlatformFavoriteButton
              v-if="comic"
              variant="text"
              type="comics"
              :item="comic"
          />
        </template>
        <button
            v-if="continueReading"
            type="button"
            class="continue-reading"
            @click="router.push(continueReading.route)"
        >
          <el-icon><VideoPlay/></el-icon>
          <span>继续阅读</span>
          <strong>{{ continueReading.subtitle }}</strong>
        </button>
        <div class="access-stats">
          <span>访问数量<strong>{{ comic.viewCount }} 次</strong></span>
          <span>访客数量<strong>{{ comic.visitorCount }} 人</strong></span>
        </div>
        <section v-if="chapters.length" class="comic-chapters">
          <h2>章节</h2>
          <div class="chapter-list">
            <button
                v-for="item in chapters"
                :key="item.id"
                type="button"
                class="chapter-item"
                @click="router.push(`/platform/comics/${comic.id}/chapters/${item.id}`)"
            >
              <span class="chapter-no">{{ item.chapterNo }}</span>
              <span class="chapter-info">
                <strong>{{ item.title }}</strong>
                <small>{{ item.date }} · {{ item.pages?.length || 0 }} 页</small>
                <span v-if="item.summary">{{ item.summary }}</span>
              </span>
              <el-icon><ArrowRight/></el-icon>
            </button>
          </div>
        </section>
        <el-empty v-else description="章节待上传" :image-size="90"/>
        <PlatformCommentSection
            v-if="comic"
            content-type="comics"
            :content-id="comic.id"
        />
        <PlatformRelatedContent :items="comic.related || []"/>
      </PlatformDetailLayout>
    </div>

    <el-empty v-if="!loading && !comic && !loadFailed" description="漫画不存在"/>
    <el-alert
        v-if="loadFailed"
        title="漫画详情加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import PlatformDetailLayout from '@/components/platform/PlatformDetailLayout.vue'
import {getComic, getReadingProgress} from '@/api/platform'
import {setPageSeo} from '@/util/seo'
import PlatformFavoriteButton from '@/components/platform/PlatformFavoriteButton.vue'
import PlatformCommentSection from '@/components/platform/PlatformCommentSection.vue'
import PlatformRelatedContent from '@/components/platform/PlatformRelatedContent.vue'
import {useHistoryStore} from '@/store/modules/history'

const route = useRoute()
const router = useRouter()
const comic = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const historyStore = useHistoryStore()
const comicTags = computed(() => (comic.value ? [comic.value.status, `${comic.value.chapters} 话`] : []))
const comicMeta = computed(() => (comic.value ? ['漫画作品'] : []))
const chapters = computed(() => (
  [...(comic.value?.chapterList || [])].sort((left, right) => left.chapterNo - right.chapterNo)
))
const continueReading = computed(() => {
  if (!comic.value || !serverProgress.value) return null
  const chapter = chapters.value.find(item => item.id === serverProgress.value.chapterId)
  if (!chapter) return null
  return {
    route: `/platform/comics/${comic.value.id}/chapters/${chapter.id}`,
    subtitle: `${chapter.chapterNo} 话 ${chapter.title}`
  }
})

const serverProgress = ref(null)

const loadComic = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    comic.value = await getComic(route.params.id)
    setPageSeo({
      title: `漫画 · ${comic.value.title}`,
      description: comic.value.desc,
      image: comic.value.cover
    })
    historyStore.record(
        'comics',
        comic.value,
        `/platform/comics/${route.params.id}`,
        '',
        {preserveRoute: true, preserveSubtitle: true}
    )
    getReadingProgress(route.params.id).then(progress => {
      serverProgress.value = progress
    }).catch(() => {})
  } catch (error) {
    comic.value = null
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadComic)
</script>

<style scoped lang="scss">
.continue-reading {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 48px;
  margin-bottom: 14px;
  padding: 10px 14px;
  border: 1px solid rgba(5, 150, 105, .35);
  border-radius: 5px;
  background: #ecfdf5;
  color: #047857;
  font-size: 15px;
  cursor: pointer;

  strong {
    overflow: hidden;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &:hover {
    background: #d1fae5;
  }
}

.access-stats {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 14px;

  span {
    display: block;
    padding: 10px 12px;
    border: 1px solid var(--sx-line);
    border-radius: 5px;
    background: var(--sx-canvas);
    color: var(--sx-muted);
    font-size: 13px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: var(--sx-title);
    font-size: 18px;
    line-height: 26px;
  }
}

.comic-detail-panel {
  width: 100%;
  max-width: 100%;
}

.comic-chapters {
  h2 {
    margin: 0 0 14px;
    color: var(--sx-title);
    font-size: 20px;
    line-height: 28px;
  }
}

.chapter-list {
  display: grid;
  gap: 10px;
}

.chapter-item {
  display: grid;
  align-items: center;
  grid-template-columns: 38px minmax(0, 1fr) auto;
  gap: 14px;
  width: 100%;
  min-height: 66px;
  padding: 12px 14px;
  border: 1px solid var(--sx-line);
  border-radius: 5px;
  background: var(--sx-canvas);
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color .2s ease, background-color .2s ease;

  &:hover {
    border-color: var(--sx-primary);
    background: var(--sx-surface);

    .el-icon {
      color: var(--sx-primary);
    }
  }
}

.chapter-no {
  align-self: start;
  min-width: 34px;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--sx-primary-soft);
  color: var(--sx-primary);
  font-size: 13px;
  line-height: 20px;
  text-align: center;
}

.chapter-info {
  display: block;
  min-width: 0;

  strong,
  span {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  strong {
    color: var(--sx-title);
    font-size: 16px;
    line-height: 24px;
  }

  small {
    display: block;
    margin: 3px 0 5px;
    color: var(--sx-muted);
    font-size: 13px;
    line-height: 18px;
  }

  span {
    color: var(--sx-body);
    font-size: 14px;
    line-height: 22px;
  }
}
</style>
