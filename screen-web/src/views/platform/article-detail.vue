<template>
  <div class="platform-page platform-page--detail article-detail">
    <div
        v-loading="loading"
        class="article-layout"
        :class="{'article-layout--with-toc': tableOfContents.length}"
    >
      <article v-if="article" class="article-panel">
        <header class="article-header">
          <div class="article-actions">
            <button class="article-back" type="button" @click="router.push('/platform/articles')">
              <el-icon><ArrowLeft/></el-icon>
              <span>返回列表</span>
            </button>
            <div class="article-action-group">
              <PlatformShareButton :title="article.title"/>
            </div>
          </div>
          <div class="article-title-row">
            <h1 class="article-title">{{ article.title }}</h1>
            <PlatformFavoriteButton
                v-if="article"
                class="article-favorite"
                variant="text"
                type="articles"
                :item="article"
            />
          </div>
          <div class="article-meta">
            <span>{{ article.date }}</span>
            <span>访问 {{ article.viewCount }} 次</span>
            <span>访客 {{ article.visitorCount }} 人</span>
            <span>约 {{ readingTime }} 分钟</span>
          </div>
          <p v-if="article.desc" class="article-summary">{{ article.desc }}</p>
          <div v-if="article.tags?.length" class="article-tags">
            <span v-for="tag in article.tags" :key="tag" class="platform-tag">{{ tag }}</span>
          </div>
        </header>

        <div ref="articleScroll" class="article-scroll scrollBar">
          <div
              v-if="renderedContent"
              ref="articleBody"
              class="article-body"
              v-html="renderedContent"
          ></div>
          <el-empty v-else description="正文待补充" :image-size="100"/>

          <nav v-if="article.previous || article.next" class="article-neighbor">
            <button
                v-if="article.previous"
                type="button"
                @click="openArticle(article.previous.id)"
            >
              <small>上一篇</small>
              <strong>{{ article.previous.title }}</strong>
            </button>
            <button
                v-if="article.next"
                type="button"
                class="next"
                @click="openArticle(article.next.id)"
            >
              <small>下一篇</small>
              <strong>{{ article.next.title }}</strong>
            </button>
          </nav>

          <footer v-if="article.related?.length" class="article-related">
            <h2>相关阅读</h2>
            <button
                v-for="item in article.related"
                :key="item.id"
                type="button"
                @click="openArticle(item.id)"
            >
              <strong>{{ item.title }}</strong>
              <span>{{ item.date }} · {{ item.viewCount }} 次阅读</span>
            </button>
          </footer>

          <PlatformCommentSection
              v-if="article"
              content-type="articles"
              :content-id="article.id"
          />
        </div>
      </article>

      <aside v-if="tableOfContents.length" class="article-aside">
        <div class="toc-panel">
          <h2>目录</h2>
          <a
              v-for="item in tableOfContents"
              :key="item.id"
              :href="`#${item.id}`"
              :class="[`level-${item.level}`, {active: activeHeadingId === item.id}]"
              @click.prevent="scrollToHeading(item.id)"
          >
            {{ item.title }}
          </a>
        </div>
      </aside>
    </div>

    <el-empty
        v-if="!loading && !article && !loadFailed"
        description="文章不存在"
    />
    <el-alert
        v-if="loadFailed"
        title="文章加载失败，请确认后台服务已启动"
        type="error"
        show-icon
        :closable="false"
    />
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getArticle} from '@/api/platform'
import {renderMarkdown} from '@/util/markdown'
import {setPageSeo} from '@/util/seo'
import PlatformShareButton from '@/components/platform/PlatformShareButton.vue'
import PlatformFavoriteButton from '@/components/platform/PlatformFavoriteButton.vue'
import PlatformCommentSection from '@/components/platform/PlatformCommentSection.vue'
import {useHistoryStore} from '@/store/modules/history'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const articleBody = ref(null)
const articleScroll = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const activeHeadingId = ref('')
const tableOfContents = ref([])
let scrollElement = null
const historyStore = useHistoryStore()

const renderedContent = computed(() => renderMarkdown(article.value?.content))
const readingTime = computed(() => {
  const content = article.value?.content || ''
  return Math.max(1, Math.ceil(content.replace(/\s/g, '').length / 400))
})

const loadArticle = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    article.value = await getArticle(route.params.id)
    setPageSeo({
      title: `技术分享 · ${article.value.title}`,
      description: article.value.desc,
      image: article.value.cover
    })
    historyStore.record('articles', article.value, `/platform/articles/${route.params.id}`)
  } catch (error) {
    article.value = null
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const decorateArticle = async () => {
  await nextTick()
  if (!articleBody.value) return

  const headings = Array.from(articleBody.value.querySelectorAll('h2, h3'))
  tableOfContents.value = headings.map((node, index) => {
    const id = `article-heading-${index + 1}`
    node.id = id
    return {
      id,
      level: Number(node.tagName.slice(1)),
      title: node.textContent.trim()
    }
  })

  articleBody.value.querySelectorAll('pre').forEach(node => {
    const button = document.createElement('button')
    button.className = 'code-copy'
    button.type = 'button'
    button.textContent = '复制'
    button.addEventListener('click', async () => {
      await navigator.clipboard.writeText(node.querySelector('code')?.textContent || '')
      button.textContent = '已复制'
      setTimeout(() => {
        button.textContent = '复制'
      }, 1400)
    })
    node.appendChild(button)
  })
}

const detachScrollListeners = () => {
  if (!scrollElement) return
  scrollElement.removeEventListener('scroll', updateActiveHeading)
}

const attachScrollListeners = () => {
  if (articleScroll.value === scrollElement) return
  detachScrollListeners()
  scrollElement = articleScroll.value
  scrollElement?.addEventListener('scroll', updateActiveHeading)
  updateActiveHeading()
}

const updateActiveHeading = () => {
  if (!articleBody.value || !tableOfContents.value.length) return
  const boundary = articleBody.value.getBoundingClientRect().top + 90
  let currentId = tableOfContents.value[0].id
  tableOfContents.value.forEach(item => {
    const node = document.getElementById(item.id)
    if (node && node.getBoundingClientRect().top <= boundary) {
      currentId = item.id
    }
  })
  activeHeadingId.value = currentId
}

const scrollToHeading = (id) => {
  const container = articleScroll.value
  const heading = document.getElementById(id)
  if (!container || !heading) return

  const offset = heading.getBoundingClientRect().top - container.getBoundingClientRect().top
  container.scrollTo({
    top: Math.max(0, container.scrollTop + offset - 20),
    behavior: 'smooth'
  })
}

const openArticle = (id) => {
  router.push(`/platform/articles/${id}`)
}

onMounted(loadArticle)

onBeforeUnmount(() => {
  detachScrollListeners()
})

watch(() => route.params.id, (value) => {
  if (value && value !== article.value?.id) loadArticle()
})
watch(renderedContent, decorateArticle)
watch(article, async (value) => {
  if (value) {
    await nextTick()
    attachScrollListeners()
  } else {
    detachScrollListeners()
    scrollElement = null
  }
})
watch(tableOfContents, updateActiveHeading)
</script>

<style scoped lang="scss">
.article-layout {
  position: relative;
  display: flex;
  height: 100%;
  min-height: 0;
}

.article-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  height: auto;
  max-height: 100%;
  align-self: flex-start;
  min-height: 0;
  width: 100%;
  overflow: hidden;
  padding: 40px 44px 32px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
}

.article-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 18px;

  .article-action-group {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.el-button) {
    height: 34px;
  }
}

.article-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  margin: 0;
  padding: 0 8px;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--sx-muted);
  font-size: 15px;
  cursor: pointer;
  transition: color .2s ease, background-color .2s ease;

  &:hover {
    background: var(--sx-primary-soft);
    color: var(--sx-primary);
  }
}

.article-header {
  flex: none;
}

.article-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding-right: 6px;
}

.article-aside {
  display: none;
}

.toc-panel {
  display: flex;
  flex: 1;
  flex-direction: column;
  height: 100%;
  max-height: none;
  overflow: auto;
  min-width: 220px;
  padding: 16px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);

  h2 {
    margin: 0 0 10px;
    color: var(--sx-title);
    font-size: 16px;
  }

  a {
    display: block;
    margin-top: 7px;
    color: var(--sx-muted);
    font-size: 14px;
    line-height: 22px;
    text-decoration: none;

    &.level-3 {
      padding-left: 12px;
    }

    &.active {
      color: var(--sx-primary);
      font-weight: 500;
    }
  }
}

.article-meta {
  display: flex;
  gap: 12px;
  color: var(--sx-muted);
  font-size: 14px;
  margin-top: 10px;
}

.article-title {
  margin: 0;
  color: var(--sx-title);
  font-size: 34px;
  line-height: 46px;
}

.article-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.article-favorite {
  flex: none;
}

.article-summary {
  margin: 14px 0 0;
  color: var(--sx-body);
  font-size: 16px;
  line-height: 27px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 16px;
}

.article-body {
  min-height: 420px;
  margin-top: 32px;
  color: var(--sx-body);
  font-size: 16px;
  line-height: 28px;

  :deep(h2) {
    scroll-margin-top: 88px;
    margin: 34px 0 12px;
    color: var(--sx-title);
    font-size: 23px;
    line-height: 32px;
  }

  :deep(h3) {
    scroll-margin-top: 88px;
    margin: 28px 0 10px;
    color: var(--sx-title);
    font-size: 19px;
    line-height: 32px;
  }

  :deep(p) {
    margin: 14px 0;
  }

  :deep(ul),
  :deep(ol) {
    margin: 14px 0;
    padding-left: 24px;
  }

  :deep(li) {
    margin: 7px 0;
  }

  :deep(blockquote) {
    margin: 20px 0;
    padding: 10px 16px;
    border-left: 3px solid var(--sx-primary);
    border-radius: 4px;
    background: var(--sx-primary-soft);
  }

  :deep(code) {
    padding: 2px 6px;
    border-radius: 4px;
    background: var(--sx-canvas);
    color: var(--sx-primary);
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    font-size: 14px;
  }

  :deep(pre) {
    position: relative;
    overflow-x: auto;
    margin: 20px 0;
    padding: 18px;
    border-radius: 6px;
    background: #111827;

    code {
      padding: 0;
      background: transparent;
      color: #e5e7eb;
      font-size: 14px;
      line-height: 24px;
    }
  }

  :deep(.code-copy) {
    position: absolute;
    top: 10px;
    right: 10px;
    height: 28px;
    padding: 0 9px;
    border: 1px solid rgba(255, 255, 255, .22);
    border-radius: 4px;
    background: rgba(255, 255, 255, .1);
    color: #e5e7eb;
    font-size: 12px;
    cursor: pointer;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 6px;
  }

  :deep(a) {
    color: var(--sx-primary);

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(hr) {
    margin: 26px 0;
    border-color: var(--sx-line);
  }
}

.article-neighbor {
  display: grid;
  gap: 10px;
  margin-top: 34px;
  padding-top: 20px;
  border-top: 1px solid var(--sx-line);
  grid-template-columns: repeat(2, minmax(0, 1fr));

  button {
    overflow: hidden;
    min-height: 66px;
    padding: 11px 13px;
    border: 1px solid var(--sx-line);
    border-radius: 5px;
    background: var(--sx-canvas);
    color: inherit;
    text-align: left;
    cursor: pointer;

    &.next {
      text-align: right;
    }

    small {
      display: block;
      color: var(--sx-muted);
      font-size: 12px;
      line-height: 18px;
    }

    strong {
      display: block;
      overflow: hidden;
      margin-top: 3px;
      color: var(--sx-title);
      font-size: 14px;
      line-height: 21px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.article-related {
  margin-top: 22px;

  h2 {
    margin: 0 0 10px;
    color: var(--sx-title);
    font-size: 18px;
    line-height: 26px;
  }

  button {
    display: block;
    width: 100%;
    padding: 10px 0;
    border: 0;
    border-top: 1px solid var(--sx-line);
    background: transparent;
    color: inherit;
    text-align: left;
    cursor: pointer;

    strong {
      display: block;
      overflow: hidden;
      color: var(--sx-title);
      font-size: 15px;
      font-weight: 500;
      line-height: 24px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    span {
      color: var(--sx-muted);
      font-size: 13px;
      line-height: 20px;
    }
  }
}

@media (min-width: 1200px) {
  .article-layout--with-toc .article-panel {
    padding-right: 284px;
  }

  .article-layout--with-toc .article-aside {
    display: flex;
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 2;
    width: 240px;
    padding: 20px 0;
  }
}

@media (max-width: 768px) {
  .article-panel {
    padding: 26px 20px;
  }

  .article-actions {
      margin-bottom: 14px;
  }

  .article-title {
    font-size: 26px;
    line-height: 37px;
  }

  .article-neighbor {
    grid-template-columns: 1fr;

    button.next {
      text-align: left;
    }
  }
}
</style>
