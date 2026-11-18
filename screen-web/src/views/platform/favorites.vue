<template>
  <div class="platform-page favorites-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">我的收藏</h1>
      <p class="platform-page-desc">收藏保存在当前浏览器，不会同步到其他设备</p>
    </header>

    <div class="collection-toolbar">
      <div class="type-tabs">
        <button
            v-for="item in typeTabs"
            :key="item.value"
            type="button"
            :class="{active: activeType === item.value}"
            @click="activeType = item.value"
        >
          {{ item.label }}
          <span>{{ item.count }}</span>
        </button>
      </div>
      <el-button
          v-if="favoriteStore.count"
          type="danger"
          plain
          @click="clearAll"
      >
        <el-icon><Delete/></el-icon>
        <span>清空收藏</span>
      </el-button>
    </div>

    <div v-if="filteredFavorites.length" class="favorite-grid">
      <article
          v-for="item in filteredFavorites"
          :key="`${item.type}-${item.id}`"
          class="platform-card favorite-card"
      >
        <button type="button" class="favorite-link" @click="openFavorite(item)">
          <span class="favorite-cover">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy">
            <i v-else>{{ typeLabel(item.type) }}</i>
          </span>
          <span class="favorite-info">
            <span class="favorite-type">{{ typeLabel(item.type) }}</span>
            <strong class="favorite-title">{{ item.title }}</strong>
            <span class="favorite-desc">{{ item.desc || '暂无描述' }}</span>
            <span class="favorite-meta">{{ item.meta || '暂无信息' }}</span>
            <span class="favorite-time">收藏于 {{ formatTime(item.savedAt) }}</span>
          </span>
        </button>
        <button
            type="button"
            class="remove-favorite"
            aria-label="移除收藏"
            title="移除收藏"
            @click.stop="removeFavorite(item)"
        >
          <el-icon><Close/></el-icon>
        </button>
      </article>
    </div>

    <el-empty
        v-else
        description="还没有收藏内容"
    />
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessageBox} from 'element-plus'
import {PLATFORM_FAVORITE_TYPE_LABELS, PLATFORM_FAVORITE_TYPES} from '@/const/platform'
import {useFavoriteStore} from '@/store/modules/favorite'

const router = useRouter()
const favoriteStore = useFavoriteStore()
const activeType = ref('all')

const typeTabs = computed(() => [
  {label: '全部', value: 'all', count: favoriteStore.count},
  ...Object.entries(PLATFORM_FAVORITE_TYPES).map(([, value]) => ({
    label: PLATFORM_FAVORITE_TYPE_LABELS[value],
    value,
    count: favoriteStore.items.filter(item => item.type === value).length
  }))
])

const filteredFavorites = computed(() => (
  activeType.value === 'all'
    ? favoriteStore.items
    : favoriteStore.items.filter(item => item.type === activeType.value)
))

const typeLabel = type => PLATFORM_FAVORITE_TYPE_LABELS[type] || '内容'

const formatTime = value => {
  if (!value) return '未知时间'
  return new Date(value).toLocaleString('zh-CN', {hour12: false})
}

const openFavorite = item => {
  const paths = {
    [PLATFORM_FAVORITE_TYPES.WORKS]: `/platform/works/${item.id}`,
    [PLATFORM_FAVORITE_TYPES.COMICS]: `/platform/comics/${item.id}`,
    [PLATFORM_FAVORITE_TYPES.MOVIES]: `/platform/movies/${item.id}`,
    [PLATFORM_FAVORITE_TYPES.ANIME]: `/platform/anime/${item.id}`,
    [PLATFORM_FAVORITE_TYPES.ARTICLES]: `/platform/articles/${item.id}`
  }
  router.push(paths[item.type])
}

const removeFavorite = item => {
  favoriteStore.remove(item.type, item.id)
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定清空当前浏览器的全部收藏吗？', '清空收藏', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    favoriteStore.clear()
  } catch (error) {
    // 用户取消时不需要提示
  }
}
</script>

<style scoped lang="scss">
.collection-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;

  button {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 0 11px;
    border: 1px solid var(--sx-line);
    border-radius: 4px;
    background: var(--sx-surface);
    color: var(--sx-muted);
    font-size: 14px;
    cursor: pointer;

    span {
      color: var(--sx-muted);
      font-size: 13px;
    }

    &.active {
      border-color: var(--sx-primary);
      background: var(--sx-primary-soft);
      color: var(--sx-primary);

      span {
        color: var(--sx-primary);
      }
    }
  }
}

.favorite-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
}

.favorite-card {
  position: relative;
  overflow: visible;
}

.favorite-link {
  display: grid;
  width: 100%;
  grid-template-columns: 150px minmax(0, 1fr);
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.favorite-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  border-radius: 6px 0 0 6px;
  overflow: hidden;
  background: var(--sx-cover-bg);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  i {
    color: var(--sx-muted);
    font-size: 13px;
    font-style: normal;
  }
}

.favorite-info {
  display: block;
  min-width: 0;
  padding: 18px 50px 18px 18px;
}

.favorite-type {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--sx-primary-soft);
  color: var(--sx-primary);
  font-size: 12px;
  line-height: 20px;
}

.favorite-title {
  display: block;
  overflow: hidden;
  margin-top: 7px;
  color: var(--sx-title);
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-desc {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 5px;
  color: var(--sx-body);
  font-size: 13px;
  line-height: 21px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.favorite-meta,
.favorite-time {
  display: block;
  overflow: hidden;
  margin-top: 4px;
  color: var(--sx-muted);
  font-size: 12px;
  line-height: 19px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-favorite {
  position: absolute;
  top: 12px;
  right: 12px;
  display: grid;
  place-items: center;
  width: 27px;
  height: 27px;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--sx-muted);
  cursor: pointer;

  &:hover {
    background: var(--sx-canvas);
    color: var(--sx-primary);
  }
}

@media (max-width: 720px) {
  .collection-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .favorite-grid {
    grid-template-columns: 1fr;
  }

  .favorite-link {
    grid-template-columns: 112px minmax(0, 1fr);
  }

  .favorite-cover {
    min-height: 140px;
  }

  .favorite-info {
    padding: 14px 44px 14px 14px;
  }
}
</style>
