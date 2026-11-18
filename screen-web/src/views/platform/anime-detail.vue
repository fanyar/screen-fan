<template>
  <div class="platform-page platform-page--detail">
    <div v-loading="loading" class="detail-shell">
      <PlatformDetailLayout
          v-if="anime"
          :title="anime.title"
          :cover="anime.cover"
          :description="anime.note"
          :tags="animeTags"
          :meta="animeMeta"
          aspect="poster"
          @back="router.push('/platform/anime')"
      >
        <template #title-actions>
          <PlatformFavoriteButton
              v-if="anime"
              variant="text"
              type="anime"
              :item="anime"
          />
        </template>
        <template #after-tags>
          <a
              v-if="anime?.playUrl"
              class="play-button"
              :href="anime.playUrl"
              target="_blank"
              rel="noopener noreferrer"
          >
            <el-icon><CaretRight/></el-icon>
            <span>播放</span>
          </a>
        </template>
        <div class="anime-body">
          <section class="anime-facts">
            <div><span>更新状态</span><strong>{{ anime.status }}</strong></div>
            <div><span>当前集数</span><strong>{{ anime.episodes }} 集</strong></div>
            <div>
              <span>访问数量</span>
              <strong>{{ anime.viewCount }} 次</strong>
            </div>
            <div>
              <span>访客数量</span>
              <strong>{{ anime.visitorCount }} 人</strong>
            </div>
          </section>

          <section v-if="anime.review" class="anime-review">
            <h2>追番笔记</h2>
            <PlatformMarkdownContent :markdown="anime.review"/>
          </section>

          <PlatformCommentSection
              v-if="anime"
              content-type="anime"
              :content-id="anime.id"
          />

          <PlatformRelatedContent :items="anime.related || []"/>
        </div>
      </PlatformDetailLayout>
    </div>

    <el-empty v-if="!loading && !anime && !loadFailed" description="动漫不存在"/>
    <el-alert
        v-if="loadFailed"
        title="动漫详情加载失败，请确认后台服务已启动"
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
import {getAnimeDetail} from '@/api/platform'
import {setPageSeo} from '@/util/seo'
import PlatformMarkdownContent from '@/components/platform/PlatformMarkdownContent.vue'
import PlatformFavoriteButton from '@/components/platform/PlatformFavoriteButton.vue'
import PlatformCommentSection from '@/components/platform/PlatformCommentSection.vue'
import PlatformRelatedContent from '@/components/platform/PlatformRelatedContent.vue'
import {useHistoryStore} from '@/store/modules/history'

const route = useRoute()
const router = useRouter()
const anime = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const historyStore = useHistoryStore()
const animeTags = computed(() => (anime.value ? [anime.value.genre] : []))
const animeMeta = computed(() => (anime.value
    ? [`${anime.value.year} 年`, `评分 ${anime.value.rating}`, anime.value.status]
    : []))

const loadAnime = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    anime.value = await getAnimeDetail(route.params.id)
    setPageSeo({
      title: `动漫 · ${anime.value.title}`,
      description: anime.value.note || anime.value.review?.slice(0, 150),
      image: anime.value.cover
    })
    historyStore.record('anime', anime.value, `/platform/anime/${route.params.id}`)
  } catch (error) {
    anime.value = null
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadAnime)
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

.anime-body {
  color: var(--sx-body);
  font-size: 16px;
  line-height: 28px;
}

.anime-facts {
  display: grid;
  gap: 14px 20px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  padding-bottom: 4px;

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

.anime-review {
  margin-top: 24px;

  h2 {
    margin: 0 0 12px;
    color: var(--sx-title);
    font-size: 20px;
    line-height: 28px;
  }
}

@media (max-width: 700px) {
  .anime-facts {
    grid-template-columns: 1fr;
  }
}
</style>
