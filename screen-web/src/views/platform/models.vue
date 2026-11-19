<template>
  <div class="platform-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">模型</h1>
      <p class="platform-page-desc">桥梁与工程构件三维模型库</p>
    </header>

    <PlatformListToolbar
        v-model:keyword="query.keyword"
        keyword-placeholder="搜索模型、描述或标签"
        :filters="toolbarFilters"
        @update:filter="updateFilter"
        @search="search"
        @reset="reset"
    />

    <div v-loading="loading" class="model-grid">
      <button
          v-for="item in models"
          :key="item.id"
          type="button"
          class="platform-card model-card"
          @click="openDetail(modelRoute(item))"
      >
        <span class="model-cover">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy">
          <span v-else class="model-cover__fallback">{{ item.title }}</span>
          <span class="platform-badge model-badge">{{ item.category }}</span>
        </span>
        <span class="model-body">
          <span class="platform-card__title">{{ item.title }}</span>
          <span class="platform-card__meta">更新时间：{{ formatTime(item.updatedAt) }}</span>
          <span class="platform-card__desc">{{ item.desc }}</span>
          <span class="model-tags">
            <span v-for="tag in item.tags" :key="tag" class="platform-tag">{{ tag }}</span>
          </span>
        </span>
      </button>
    </div>

    <el-empty v-if="!loading && !loadFailed && !models.length" description="暂已发布模型"/>
    <el-alert
        v-if="loadFailed"
        title="模型数据加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />

    <div v-if="total" class="list-footer">
      <span class="list-total">共 {{ total }} 个模型</span>
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="PLATFORM_LIST_PAGE_SIZES"
          layout="prev, pager, next, sizes"
          :total="total"
          @current-change="loadModelList"
          @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import PlatformListToolbar from '@/components/platform/PlatformListToolbar.vue'
import {getModelFacets, getModels} from '@/api/platform'
import {useRouteListQuery} from '@/composables/useRouteListQuery'
import {MODEL_DEFAULT_PAGE_SIZE} from '@/const/model'
import {PLATFORM_LIST_PAGE_SIZES} from '@/const/platform'

const models = ref([])
const facets = ref({categories: []})
const loading = ref(false)
const loadFailed = ref(false)
const total = ref(0)
const {
  query,
  syncRouteQuery,
  openDetail
} = useRouteListQuery({
  keyword: '',
  category: '',
  page: 1,
  pageSize: MODEL_DEFAULT_PAGE_SIZE
}, PLATFORM_LIST_PAGE_SIZES)

const toolbarFilters = computed(() => [
  {name: 'category', label: '分类', value: query.category, options: facets.value.categories || []}
])

const requestParams = () => Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && value !== undefined)
)

const modelRoute = item => item.fileUrl === '/platform/models/house-tour'
  ? '/platform/models/house-tour'
  : item.fileUrl === '/platform/models/villa'
    ? '/platform/models/villa'
  : item.fileUrl === '/platform/models/suzhou-garden'
    ? '/platform/models/suzhou-garden'
  : `/platform/models/${item.id}`

const loadModelList = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getModels(requestParams())
    models.value = result.items
    total.value = result.total
  } catch (error) {
    models.value = []
    total.value = 0
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const loadFacets = async () => {
  try {
    facets.value = await getModelFacets()
  } catch (error) {
    facets.value = {categories: []}
  }
}

const updateFilter = ({name, value}) => {
  query[name] = value ?? ''
  query.page = 1
  syncRouteQuery()
  loadModelList()
}

const search = () => {
  query.page = 1
  syncRouteQuery()
  loadModelList()
}

const reset = () => {
  query.keyword = ''
  query.category = ''
  query.page = 1
  syncRouteQuery()
  loadModelList()
}

const handleSizeChange = () => {
  query.page = 1
  syncRouteQuery()
  loadModelList()
}

const formatTime = value => String(value || '').replace('T', ' ').slice(0, 16)

onMounted(() => {
  loadFacets()
  loadModelList()
})
</script>

<style scoped lang="scss">
.model-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.model-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.model-cover {
  position: relative;
  display: block;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: #101a2c;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform .3s ease;
  }
}

.model-card:hover .model-cover img {
  transform: scale(1.02);
}

.model-cover__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 20px;
  color: #94a3b8;
  font-size: 17px;
  font-weight: 600;
  text-align: center;
}

.model-badge {
  position: absolute;
  top: 10px;
  left: 10px;
}

.model-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
}

.model-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: auto;
}

.list-footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 24px;
}

.list-total {
  color: var(--sx-muted);
  font-size: 15px;
}

@media (max-width: 1100px) {
  .model-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .model-grid {
    grid-template-columns: 1fr;
  }
}
</style>
