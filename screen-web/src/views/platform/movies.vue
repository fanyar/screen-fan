<template>
  <div class="platform-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">电影</h1>
      <p class="platform-page-desc">看过且想记录的片子</p>
    </header>

    <PlatformListToolbar
        v-model:keyword="query.keyword"
        keyword-placeholder="搜索电影、类型或备注"
        :filters="toolbarFilters"
        @update:filter="updateFilter"
        @search="search"
        @reset="reset"
    />

    <div v-loading="loading" class="platform-content-grid">
      <div
          v-for="item in movies"
          :key="item.id"
          class="platform-card group cursor-pointer overflow-hidden rounded-[6px]"
          @click="router.push(`/platform/movies/${item.id}`)"
      >
        <div class="platform-cover aspect-[2/3]">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" class="h-full w-full object-cover">
          <div v-else class="platform-cover__fallback">
            <span>{{ item.title }}</span>
          </div>
          <span class="platform-badge platform-badge--rating left-[10px] top-[10px]">{{ item.rating }}</span>
          <PlatformFavoriteButton
              class="card-favorite"
              type="movies"
              :item="item"
          />
        </div>
        <div class="p-[12px]">
          <div class="platform-card__title">{{ item.title }}</div>
          <div class="platform-card__meta">{{ item.year }} · {{ item.genre }}</div>
          <p class="platform-card__desc">{{ item.note }}</p>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !loadFailed && !movies.length" description="暂无电影"/>
    <el-alert
        v-if="loadFailed"
        title="电影数据加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />

    <div v-if="total" class="list-footer">
      <span class="list-total">共 {{ total }} 部电影</span>
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="PLATFORM_LIST_PAGE_SIZES"
          layout="prev, pager, next, sizes"
          :total="total"
          @current-change="loadMovieList"
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
import {getContentFacets, getMovies} from '@/api/platform'
import {PLATFORM_LIST_PAGE_SIZES} from '@/const/platform'

const router = useRouter()
const movies = ref([])
const facets = ref({})
const loading = ref(false)
const loadFailed = ref(false)
const total = ref(0)
const query = reactive({
  keyword: '',
  genre: '',
  year: '',
  page: 1,
  pageSize: 12
})

const toolbarFilters = computed(() => [
  {name: 'genre', label: '类型', value: query.genre, options: facets.value.movieGenres || []},
  {name: 'year', label: '年份', value: query.year, options: facets.value.movieYears || []}
])

const requestParams = () => Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && value !== undefined)
)

const loadMovieList = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getMovies(requestParams())
    movies.value = result.items
    total.value = result.total
  } catch (error) {
    movies.value = []
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
  loadMovieList()
}

const search = () => {
  query.page = 1
  loadMovieList()
}

const reset = () => {
  query.keyword = ''
  query.genre = ''
  query.year = ''
  query.page = 1
  loadMovieList()
}

const handleSizeChange = () => {
  query.page = 1
  loadMovieList()
}

onMounted(() => {
  loadFacets()
  loadMovieList()
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
