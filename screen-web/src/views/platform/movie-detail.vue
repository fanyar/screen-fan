<template>
  <div class="platform-page platform-page--detail">
    <div v-loading="loading" class="detail-shell">
      <PlatformDetailLayout
          v-if="movie"
          :title="movie.title"
          :cover="movie.cover"
          :description="movie.note"
          :tags="movieTags"
          :meta="movieMeta"
          aspect="poster"
          @back="router.push('/platform/movies')"
      >
        <template #title-actions>
          <PlatformFavoriteButton
              v-if="movie"
              variant="text"
              type="movies"
              :item="movie"
          />
        </template>
        <template #after-tags>
          <a
              v-if="movie?.playUrl"
              class="play-button"
              :href="movie.playUrl"
              target="_blank"
              rel="noopener noreferrer"
          >
            <el-icon><CaretRight/></el-icon>
            <span>播放</span>
          </a>
        </template>
        <div class="movie-body">
          <section class="movie-facts">
            <div><span>观看日期</span><strong>{{ movie.watchDate || '未记录' }}</strong></div>
            <div><span>导演</span><strong>{{ movie.director || '未记录' }}</strong></div>
            <div class="movie-cast">
              <span>主要演员</span>
              <strong>{{ movie.castNames?.length ? movie.castNames.join(' · ') : '未记录' }}</strong>
            </div>
            <div>
              <span>访问数量</span>
              <strong>{{ movie.viewCount }} 次</strong>
            </div>
            <div>
              <span>访客数量</span>
              <strong>{{ movie.visitorCount }} 人</strong>
            </div>
          </section>

          <section v-if="hasScores" class="score-grid">
            <div v-for="item in scoreItems" :key="item.label" class="score-item">
              <div class="score-head">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="score-track">
                <i :style="{width: `${item.value * 10}%`}"/>
              </div>
            </div>
          </section>

          <section v-if="movie.review" class="movie-review">
            <h2>观影笔记</h2>
            <PlatformMarkdownContent :markdown="movie.review"/>
          </section>

          <PlatformCommentSection
              v-if="movie"
              content-type="movies"
              :content-id="movie.id"
          />

          <PlatformRelatedContent :items="movie.related || []"/>
        </div>
      </PlatformDetailLayout>
    </div>

    <el-empty v-if="!loading && !movie && !loadFailed" description="电影不存在"/>
    <el-alert
        v-if="loadFailed"
        title="电影详情加载失败，请确认后台服务已启动"
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
import {getMovie} from '@/api/platform'
import {setPageSeo} from '@/util/seo'
import PlatformMarkdownContent from '@/components/platform/PlatformMarkdownContent.vue'
import PlatformFavoriteButton from '@/components/platform/PlatformFavoriteButton.vue'
import PlatformCommentSection from '@/components/platform/PlatformCommentSection.vue'
import PlatformRelatedContent from '@/components/platform/PlatformRelatedContent.vue'
import {useHistoryStore} from '@/store/modules/history'

const route = useRoute()
const router = useRouter()
const movie = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const historyStore = useHistoryStore()
const movieTags = computed(() => (movie.value ? [movie.value.genre] : []))
const movieMeta = computed(() => (movie.value ? [`${movie.value.year} 年`, `评分 ${movie.value.rating}`] : []))
const scoreItems = computed(() => [
  {label: '故事', value: movie.value?.storyScore || 0},
  {label: '视觉', value: movie.value?.visualScore || 0},
  {label: '声音', value: movie.value?.soundScore || 0},
  {label: '重看', value: movie.value?.rewatchValue || 0}
])
const hasScores = computed(() => scoreItems.value.some(item => item.value > 0))

const loadMovie = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    movie.value = await getMovie(route.params.id)
    setPageSeo({
      title: `电影 · ${movie.value.title}`,
      description: movie.value.note || movie.value.review?.slice(0, 150),
      image: movie.value.cover
    })
    historyStore.record('movies', movie.value, `/platform/movies/${route.params.id}`)
  } catch (error) {
    movie.value = null
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadMovie)
</script>

<style scoped lang="scss">
.play-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 34px;
  padding: 0 14px;
  border-radius: 4px;
  background: var(--sx-primary);
  color: #fff;
  font-size: 14px;
  line-height: 20px;
  text-decoration: none;
  transition: background-color .2s ease, transform .15s ease;

  &:hover {
    background: var(--sx-primary-hover);
    transform: translateY(-1px);
  }
}

.movie-body {
  color: var(--sx-body);
  font-size: 16px;
  line-height: 28px;
}

.movie-facts {
  display: grid;
  gap: 14px 20px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  padding-bottom: 4px;
}

.movie-cast {
  grid-column: 1 / -1;
}

.movie-facts {
  span {
    display: block;
    color: var(--sx-muted);
    font-size: 14px;
    line-height: 22px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: var(--sx-title);
    font-size: 16px;
    line-height: 24px;
    overflow-wrap: anywhere;
  }
}

.score-grid {
  display: grid;
  gap: 12px;
  margin-top: 20px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.score-item {
  .score-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    color: var(--sx-muted);
    font-size: 14px;

    strong {
      color: var(--sx-primary);
      font-size: 18px;
    }
  }
}

.score-track {
  overflow: hidden;
  height: 6px;
  margin-top: 7px;
  border-radius: 3px;
  background: var(--sx-canvas);

  i {
    display: block;
    height: 100%;
    border-radius: 3px;
    background: var(--sx-primary);
  }
}

.movie-review {
  margin-top: 24px;

  h2 {
    margin: 0 0 12px;
    color: var(--sx-title);
    font-size: 20px;
    line-height: 28px;
  }
}

@media (max-width: 700px) {
  .movie-facts,
  .score-grid {
    grid-template-columns: 1fr;
  }
}
</style>
