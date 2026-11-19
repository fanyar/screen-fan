<template>
  <div class="platform-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">技术分享</h1>
      <p class="platform-page-desc">工程实践与踩坑记录</p>
    </header>

    <PlatformListToolbar
        v-model:keyword="query.keyword"
        keyword-placeholder="搜索文章、摘要或标签"
        :filters="toolbarFilters"
        @update:filter="updateFilter"
        @search="search"
        @reset="reset"
    />

    <div v-loading="loading" class="flex flex-col gap-[14px]">
      <div
          v-for="item in articles"
          :key="item.id"
          class="platform-card cursor-pointer rounded-[6px] p-[20px]"
          @click="openDetail(`/platform/articles/${item.id}`)"
      >
        <div class="article-card-head">
          <div class="platform-card__title">{{ item.title }}</div>
          <div class="platform-card__meta !mt-0">{{ item.date }} · {{ item.viewCount }} 次阅读</div>
        </div>
        <p class="platform-card__desc">{{ item.desc }}</p>
        <div class="mt-[10px] flex flex-wrap gap-[6px]">
          <span v-for="tag in item.tags" :key="tag" class="platform-tag">{{ tag }}</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !loadFailed && !articles.length" description="暂无文章"/>
    <el-alert
        v-if="loadFailed"
        title="技术分享数据加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />

    <div v-if="total" class="list-footer">
      <span class="list-total">共 {{ total }} 篇文章</span>
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="PLATFORM_LIST_PAGE_SIZES"
          layout="prev, pager, next, sizes"
          :total="total"
          @current-change="loadArticleList"
          @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import PlatformListToolbar from '@/components/platform/PlatformListToolbar.vue'
import {getArticles, getContentFacets} from '@/api/platform'
import {useRouteListQuery} from '@/composables/useRouteListQuery'
import {PLATFORM_LIST_PAGE_SIZES} from '@/const/platform'

const articles = ref([])
const facets = ref({})
const loading = ref(false)
const loadFailed = ref(false)
const total = ref(0)
const {
  query,
  syncRouteQuery,
  openDetail
} = useRouteListQuery({
  keyword: '',
  tag: '',
  year: '',
  page: 1,
  pageSize: 6
}, PLATFORM_LIST_PAGE_SIZES)

const toolbarFilters = computed(() => [
  {name: 'tag', label: '标签', value: query.tag, options: facets.value.articleTags || []},
  {name: 'year', label: '年份', value: query.year, options: facets.value.articleYears || []}
])

const requestParams = () => Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && value !== undefined)
)

const loadArticleList = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const result = await getArticles(requestParams())
    articles.value = result.items
    total.value = result.total
  } catch (error) {
    articles.value = []
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
  syncRouteQuery()
  loadArticleList()
}

const search = () => {
  query.page = 1
  syncRouteQuery()
  loadArticleList()
}

const reset = () => {
  query.keyword = ''
  query.tag = ''
  query.year = ''
  query.page = 1
  syncRouteQuery()
  loadArticleList()
}

const handleSizeChange = () => {
  query.page = 1
  syncRouteQuery()
  loadArticleList()
}

onMounted(() => {
  loadFacets()
  loadArticleList()
})
</script>

<style scoped lang="scss">
.article-card-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.article-card-head .platform-card__title {
  min-width: 0;
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

@media (max-width: 720px) {
  .article-card-head {
    display: block;
  }

  .article-card-head .platform-card__meta {
    margin-top: 4px;
  }
}
</style>
