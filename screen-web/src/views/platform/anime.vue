<template>
  <div class="platform-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">动漫</h1>
      <p class="platform-page-desc">追番记录与更新状态</p>
    </header>

    <PlatformListToolbar
        v-model:keyword="query.keyword"
        keyword-placeholder="搜索动漫、类型或备注"
        :filters="toolbarFilters"
        @update:filter="updateFilter"
        @search="search"
        @reset="reset"
    />

    <div v-loading="loading" class="platform-content-grid">
      <div
          v-for="item in anime"
          :key="item.id"
          class="platform-card group cursor-pointer overflow-hidden rounded-[6px]"
          @click="router.push(`/platform/anime/${item.id}`)"
      >
        <div class="platform-cover aspect-[2/3]">
          <img
              v-if="item.cover"
              :src="item.cover"
              :alt="item.title"
              referrerpolicy="no-referrer"
              class="h-full w-full object-cover"
              @error="item.cover = ''"
          >
          <div v-else class="platform-cover__fallback">
            <span>{{ item.title }}</span>
          </div>
          <span class="platform-badge platform-badge--rating left-[10px] top-[10px]">{{ item.rating }}</span>
          <span class="anime-status">{{ item.status }}</span>
          <PlatformFavoriteButton
              class="card-favorite"
              type="anime"
              :item="item"
          />
        </div>
        <div class="p-[12px]">
          <div class="platform-card__title">{{ item.title }}</div>
          <div class="platform-card__meta">{{ item.year }} · {{ item.genre }} · {{ item.episodes }} 集</div>
          <p class="platform-card__desc">{{ item.note }}</p>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !loadFailed && !anime.length" description="暂无动漫"/>
    <el-alert
        v-if="loadFailed"
        title="动漫数据加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />

    <div v-if="total" class="list-footer">
      <span class="list-total">共 {{ total }} 部动漫</span>
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="PLATFORM_LIST_PAGE_SIZES"
          layout="prev, pager, next, sizes"
          :total="total"
          @current-change="loadAnimeList"
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
import {getAnime, getContentFacets} from '@/api/platform'
import {PLATFORM_LIST_PAGE_SIZES} from '@/const/platform'

const router = useRouter()
const anime = ref([])
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
  {name: 'genre', label: '类型', value: query.genre, options: facets.value.animeGenres || []},
  {name: 'year', label: '年份', value: query.year, options: facets.value.animeYears || []}
])

const requestParams = () => Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && value !== undefined)
)

const loadAnimeList = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getAnime(requestParams())
    anime.value = result.items
    total.value = result.total
  } catch (error) {
    anime.value = []
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
  loadAnimeList()
}

const search = () => {
  query.page = 1
  loadAnimeList()
}

const reset = () => {
  query.keyword = ''
  query.genre = ''
  query.year = ''
  query.page = 1
  loadAnimeList()
}

const handleSizeChange = () => {
  query.page = 1
  loadAnimeList()
}

onMounted(() => {
  loadFacets()
  loadAnimeList()
})
</script>

<style scoped lang="scss">
.card-favorite {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.anime-status {
  position: absolute;
  bottom: 10px;
  left: 10px;
  z-index: 1;
  max-width: calc(100% - 52px);
  padding: 3px 7px;
  border-radius: 4px;
  background: rgba(15, 23, 42, .78);
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
