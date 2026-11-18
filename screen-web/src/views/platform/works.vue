<template>
  <div class="platform-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">作品</h1>
      <p class="platform-page-desc">大屏可视化与 Web 项目集</p>
    </header>

    <PlatformListToolbar
        v-model:keyword="query.keyword"
        keyword-placeholder="搜索作品、描述或标签"
        :filters="toolbarFilters"
        @update:filter="updateFilter"
        @search="search"
        @reset="reset"
    />

    <div v-loading="loading" class="grid grid-cols-1 gap-[20px] md:grid-cols-2">
      <div
          v-for="item in works"
          :key="item.id"
          class="platform-card group cursor-pointer overflow-hidden rounded-[6px]"
          @click="router.push(`/platform/works/${item.id}`)"
      >
        <div class="platform-cover aspect-[16/9]">
          <img
              v-if="item.cover"
              :src="item.cover"
              :alt="item.title"
              class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.02]"
          >
          <div v-else class="platform-cover__fallback">
            <span>{{ item.title }}</span>
          </div>
          <span class="platform-badge left-[10px] top-[10px]">{{ item.year }}</span>
          <PlatformFavoriteButton
              class="card-favorite"
              type="works"
              :item="item"
          />
        </div>
        <div class="p-[16px]">
          <div class="platform-card__title">{{ item.title }}</div>
          <p class="platform-card__desc">{{ item.desc }}</p>
          <div class="mt-[10px] flex flex-wrap gap-[6px]">
            <span v-for="tag in item.tags" :key="tag" class="platform-tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !loadFailed && !works.length" description="暂无作品"/>
    <el-alert
        v-if="loadFailed"
        title="作品数据加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />

    <div v-if="total" class="list-footer">
      <span class="list-total">共 {{ total }} 个作品</span>
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="PLATFORM_LIST_PAGE_SIZES"
          layout="prev, pager, next, sizes"
          :total="total"
          @current-change="loadWorkList"
          @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import PlatformListToolbar from '@/components/platform/PlatformListToolbar.vue'
import PlatformFavoriteButton from '@/components/platform/PlatformFavoriteButton.vue'
import {getContentFacets, getWorks} from '@/api/platform'
import {PLATFORM_LIST_PAGE_SIZES} from '@/const/platform'

const router = useRouter()
const works = ref([])
const facets = ref({})
const loading = ref(false)
const loadFailed = ref(false)
const total = ref(0)
const query = reactive({
  keyword: '',
  tag: '',
  year: '',
  page: 1,
  pageSize: 6
})

const toolbarFilters = computed(() => [
  {name: 'tag', label: '标签', value: query.tag, options: facets.value.workTags || []},
  {name: 'year', label: '年份', value: query.year, options: facets.value.workYears || []}
])

const requestParams = () => Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && value !== undefined)
)

const loadWorkList = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getWorks(requestParams())
    works.value = result.items
    total.value = result.total
  } catch (error) {
    works.value = []
    total.value = 0
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const loadFacets = async () => {
  try {
    facets.value = await getContentFacets()
  } catch (error) {
    facets.value = {}
  }
}

const updateFilter = ({name, value}) => {
  query[name] = value ?? ''
  query.page = 1
  loadWorkList()
}

const search = () => {
  query.page = 1
  loadWorkList()
}

const reset = () => {
  query.keyword = ''
  query.tag = ''
  query.year = ''
  query.page = 1
  loadWorkList()
}

const handleSizeChange = () => {
  query.page = 1
  loadWorkList()
}

onMounted(() => {
  loadFacets()
  loadWorkList()
})
</script>

<style scoped lang="scss">
.card-favorite {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
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
</style>
