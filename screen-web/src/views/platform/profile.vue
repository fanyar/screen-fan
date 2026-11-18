<template>
  <div class="platform-page profile-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">个人中心</h1>
      <p class="platform-page-desc">你的收藏、浏览记录和阅读进度都在这里</p>
    </header>

    <div class="profile-stats">
      <div class="stat-card">
        <span class="stat-value">{{ favoriteStore.count }}</span>
        <span class="stat-label">收藏</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ historyStore.count }}</span>
        <span class="stat-label">浏览记录</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ readingCount }}</span>
        <span class="stat-label">阅读中</span>
      </div>
    </div>

    <div class="profile-tabs">
      <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="{active: activeTab === tab.key}"
          @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="activeTab === 'favorites'" class="profile-content">
      <div v-if="favoriteStore.favorites.length" class="profile-list">
        <button
            v-for="item in favoriteStore.favorites"
            :key="`${item.type}-${item.id}`"
            type="button"
            class="profile-item"
            @click="navigate(item)"
        >
          <div class="profile-item__cover">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy">
            <span v-else class="profile-item__fallback">{{ item.title.charAt(0) }}</span>
          </div>
          <div class="profile-item__body">
            <span class="profile-item__title">{{ item.title }}</span>
            <small v-if="item.meta" class="profile-item__meta">{{ item.meta }}</small>
          </div>
          <el-tag size="small">{{ typeLabel(item.type) }}</el-tag>
        </button>
      </div>
      <el-empty v-else description="暂无收藏"/>
    </div>

    <div v-else-if="activeTab === 'history'" class="profile-content">
      <div v-if="historyStore.history.length" class="profile-list">
        <button
            v-for="item in historyStore.history"
            :key="`h-${item.type}-${item.id}`"
            type="button"
            class="profile-item"
            @click="navigate(item)"
        >
          <div class="profile-item__cover">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy">
            <span v-else class="profile-item__fallback">{{ item.title.charAt(0) }}</span>
          </div>
          <div class="profile-item__body">
            <span class="profile-item__title">{{ item.title }}</span>
            <small v-if="item.subtitle" class="profile-item__meta">{{ item.subtitle }}</small>
          </div>
          <el-tag size="small" type="info">{{ typeLabel(item.type) }}</el-tag>
        </button>
      </div>
      <el-empty v-else description="暂无浏览记录"/>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useFavoriteStore} from '@/store/modules/favorite'
import {useHistoryStore} from '@/store/modules/history'
import {PLATFORM_FAVORITE_TYPE_LABELS, PLATFORM_FAVORITE_TYPES} from '@/const/platform'
import {getVisitorKey} from '@/util/visitor'

const router = useRouter()
const favoriteStore = useFavoriteStore()
const historyStore = useHistoryStore()

const activeTab = ref('favorites')
const tabs = [
  {key: 'favorites', label: '收藏'},
  {key: 'history', label: '浏览记录'}
]

const readingCount = computed(() => {
  return historyStore.history.filter(item => item.type === PLATFORM_FAVORITE_TYPES.COMICS && item.route?.includes('/chapters/')).length
})

const typeLabel = (type) => PLATFORM_FAVORITE_TYPE_LABELS[type] || type

const navigate = (item) => {
  if (item.route) {
    router.push(item.route)
    return
  }
  const basePath = {
    [PLATFORM_FAVORITE_TYPES.WORKS]: '/platform/works',
    [PLATFORM_FAVORITE_TYPES.COMICS]: '/platform/comics',
    [PLATFORM_FAVORITE_TYPES.MOVIES]: '/platform/movies',
    [PLATFORM_FAVORITE_TYPES.ANIME]: '/platform/anime',
    [PLATFORM_FAVORITE_TYPES.ARTICLES]: '/platform/articles'
  }
  const base = basePath[item.type]
  if (base) router.push(`${base}/${item.id}`)
}

onMounted(() => {
  getVisitorKey()
})
</script>

<style scoped lang="scss">
.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 20px 14px;
  border: 1px solid var(--sx-line);
  border-radius: 8px;
  background: var(--sx-surface);
}

.stat-value {
  color: var(--sx-primary);
  font-size: 28px;
  font-weight: 600;
  line-height: 1;
}

.stat-label {
  color: var(--sx-muted);
  font-size: 14px;
}

.profile-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 18px;

  button {
    padding: 8px 18px;
    border: 1px solid var(--sx-line);
    border-radius: 6px;
    background: var(--sx-surface);
    color: var(--sx-muted);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;

    &.active {
      border-color: var(--sx-primary);
      background: var(--sx-primary-soft, rgba(37, 99, 235, 0.08));
      color: var(--sx-primary);
      font-weight: 500;
    }
  }
}

.profile-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border: 1px solid var(--sx-line);
  border-radius: 8px;
  background: var(--sx-surface);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: var(--sx-primary);
  }
}

.profile-item__cover {
  flex-shrink: 0;
  width: 64px;
  height: 42px;
  border-radius: 4px;
  overflow: hidden;
  background: var(--sx-soft);

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.profile-item__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--sx-muted);
  font-size: 18px;
  font-weight: 500;
}

.profile-item__body {
  flex: 1;
  min-width: 0;
}

.profile-item__title {
  display: block;
  color: var(--sx-title);
  font-size: 15px;
  font-weight: 500;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-item__meta {
  display: block;
  margin-top: 2px;
  color: var(--sx-muted);
  font-size: 13px;
}

@media (max-width: 600px) {
  .profile-stats {
    grid-template-columns: 1fr;
  }
}
</style>
