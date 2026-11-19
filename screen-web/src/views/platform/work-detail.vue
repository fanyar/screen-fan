<template>
  <div class="platform-page platform-page--detail">
    <div v-loading="loading" class="detail-shell">
      <PlatformDetailLayout
          v-if="work"
          :title="work.title"
          :cover="work.cover"
          :description="work.desc"
          :tags="work.tags"
          :meta="workMeta"
          aspect="wide"
          @back="returnToList('/platform/works')"
      >
        <template #title-actions>
          <PlatformFavoriteButton
              v-if="work"
              variant="text"
              type="works"
              :item="work"
          />
        </template>
        <div class="work-body">
          <section class="work-facts">
            <div>
              <span>项目角色</span>
              <strong>{{ work.role || '未填写' }}</strong>
            </div>
            <div>
              <span>创作周期</span>
              <strong>{{ work.period || '未填写' }}</strong>
            </div>
            <div>
              <span>技术栈</span>
              <strong>{{ work.techStack?.length ? work.techStack.join(' · ') : '未填写' }}</strong>
            </div>
            <div v-if="work.projectUrl">
              <span>项目链接</span>
              <strong>
                <a :href="work.projectUrl" target="_blank" rel="noopener noreferrer">查看项目</a>
              </strong>
            </div>
            <div>
              <span>访问数量</span>
              <strong>{{ work.viewCount }} 次</strong>
            </div>
            <div>
              <span>访客数量</span>
              <strong>{{ work.visitorCount }} 人</strong>
            </div>
          </section>

          <section v-if="work.content" class="work-section">
            <h2>项目过程</h2>
            <PlatformMarkdownContent :markdown="work.content"/>
          </section>

          <section v-if="work.result" class="work-section">
            <h2>成果与价值</h2>
            <PlatformMarkdownContent :markdown="work.result"/>
          </section>

          <section v-if="work.images?.length" class="work-section">
            <h2>截图</h2>
            <div class="work-gallery">
              <button
                  v-for="(image, index) in work.images"
                  :key="image"
                  type="button"
                  @click="openGallery(index)"
              >
                <img :src="image" :alt="`${work.title} 截图 ${index + 1}`" loading="lazy">
              </button>
            </div>
          </section>

          <PlatformCommentSection
              v-if="work"
              content-type="works"
              :content-id="work.id"
          />

          <PlatformRelatedContent :items="work.related || []"/>
        </div>
      </PlatformDetailLayout>
    </div>
    <el-image-viewer
        v-if="galleryIndex >= 0"
        :url-list="work?.images || []"
        :initial-index="galleryIndex"
        @close="closeGallery"
    />

    <el-empty v-if="!loading && !work && !loadFailed" description="作品不存在"/>
    <el-alert
        v-if="loadFailed"
        title="作品详情加载失败，请确认后台服务已启动"
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
import PlatformMarkdownContent from '@/components/platform/PlatformMarkdownContent.vue'
import PlatformFavoriteButton from '@/components/platform/PlatformFavoriteButton.vue'
import PlatformCommentSection from '@/components/platform/PlatformCommentSection.vue'
import PlatformRelatedContent from '@/components/platform/PlatformRelatedContent.vue'
import {getWork} from '@/api/platform'
import {setPageSeo} from '@/util/seo'
import {preservedListRouteQuery} from '@/util/list-query'
import {useHistoryStore} from '@/store/modules/history'

const route = useRoute()
const router = useRouter()
const work = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const historyStore = useHistoryStore()
const galleryIndex = ref(-1)
const workMeta = computed(() => {
  if (!work.value) return []
  return [`${work.value.year} 年`, work.value.role, work.value.period].filter(Boolean)
})

const returnToList = path => router.push({
  path,
  query: preservedListRouteQuery(route.query)
})

const loadWork = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    work.value = await getWork(route.params.id)
    setPageSeo({
      title: `作品 · ${work.value.title}`,
      description: work.value.desc,
      image: work.value.cover
    })
    historyStore.record('works', work.value, `/platform/works/${route.params.id}`)
  } catch (error) {
    work.value = null
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const openGallery = (index) => {
  galleryIndex.value = index
}

const closeGallery = () => {
  galleryIndex.value = -1
}

onMounted(loadWork)
</script>

<style scoped lang="scss">
.work-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 20px;
  padding: 16px 0 4px;
  border-bottom: 1px solid var(--sx-line);

  span {
    display: block;
    color: var(--sx-muted);
    font-size: 14px;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: var(--sx-title);
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    overflow-wrap: anywhere;
  }
}

.work-section {
  margin-top: 24px;

  h2 {
    margin: 0 0 12px;
    color: var(--sx-title);
    font-size: 20px;
    line-height: 28px;
  }
}

.work-gallery {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;

  img {
    display: block;
    width: 100%;
    aspect-ratio: 16 / 9;
    border: 1px solid var(--sx-line);
    border-radius: 6px;
    object-fit: cover;
    transition: border-color 0.2s ease;
  }

  button {
    padding: 0;
    border: 0;
    background: transparent;
    cursor: zoom-in;
  }

  button:hover img {
    border-color: var(--sx-primary);
  }
}

@media (max-width: 700px) {
  .work-facts,
  .work-gallery {
    grid-template-columns: 1fr;
  }
}
</style>
