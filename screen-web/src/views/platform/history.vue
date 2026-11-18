<template>
  <div class="platform-page history-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">浏览历史</h1>
      <p class="platform-page-desc">最近浏览保存在当前浏览器，可用于快速回到上次位置</p>
    </header>

    <div class="history-toolbar">
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
          v-if="historyStore.count"
          type="danger"
          plain
          @click="clearAll"
      >
        <el-icon><Delete/></el-icon>
        <span>清空历史</span>
      </el-button>
    </div>

    <div v-if="filteredHistory.length" class="history-grid">
      <article
          v-for="item in filteredHistory"
          :key="`${item.type}-${item.id}`"
          class="platform-card history-card"
      >
        <button type="button" class="history-link" @click="openHistory(item)">
          <span class="history-cover">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy">
            <i v-else>{{ typeLabel(item.type) }}</i>
          </span>
          <span class="history-info">
            <span class="history-type">{{ typeLabel(item.type) }}</span>
            <strong class="history-title">{{ item.title }}</strong>
            <span v-if="item.subtitle" class="history-progress">
              <el-icon><VideoPlay/></el-icon>
              {{ item.subtitle }}
            </span>
            <span class="history-desc">{{ item.desc || '暂无描述' }}</span>
            <span class="history-meta">{{ item.meta || '暂无信息' }}</span>
            <span class="history-time">浏览于 {{ formatTime(item.visitedAt) }}</span>
          </span>
        </button>
        <button
            type="button"
            class="remove-history"
            aria-label="删除历史"
            title="删除历史"
            @click.stop="removeHistory(item)"
        >
          <el-icon><Close/></el-icon>
        </button>
      </article>
    </div>

    <el-empty v-else description="暂无浏览记录"/>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessageBox} from 'element-plus'
import {PLATFORM_FAVORITE_TYPE_LABELS, PLATFORM_FAVORITE_TYPES} from '@/const/platform'
import {useHistoryStore} from '@/store/modules/history'

const router = useRouter()
const historyStore = useHistoryStore()
const activeType = ref('all')

const typeTabs = computed(() => [
  {label: '全部', value: 'all', count: historyStore.count},
  ...Object.entries(PLATFORM_FAVORITE_TYPES).map(([, value]) => ({
    label: PLATFORM_FAVORITE_TYPE_LABELS[value],
    value,
    count: historyStore.items.filter(item => item.type === value).length
  }))
])

const filteredHistory = computed(() => (
  activeType.value === 'all'
    ? historyStore.items
    : historyStore.items.filter(item => item.type === activeType.value)
))

const typeLabel = type => PLATFORM_FAVORITE_TYPE_LABELS[type] || '内容'

const formatTime = value => {
  if (!value) return '未知时间'
  return new Date(value).toLocaleString('zh-CN', {hour12: false})
}

const openHistory = item => {
  router.push(item.route || `/platform/${item.type}/${item.id}`)
}

const removeHistory = item => {
  historyStore.remove(item.type, item.id)
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定清空当前浏览器的全部浏览历史吗？', '清空历史', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    historyStore.clear()
  } catch (error) {
    // 用户取消时不需要提示
  }
}
</script>

<style scoped lang="scss">
.history-toolbar {
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

.history-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.history-card {
  position: relative;
  overflow: visible;
}

.history-link {
  display: grid;
  width: 100%;
  grid-template-columns: 108px minmax(0, 1fr);
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.history-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 108px;
  border-radius: 6px 0 0 6px;
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

.history-info {
  display: block;
  min-width: 0;
  padding: 14px 44px 14px 14px;
}

.history-type {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--sx-primary-soft);
  color: var(--sx-primary);
  font-size: 12px;
  line-height: 20px;
}

.history-title {
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

.history-progress {
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  margin-top: 5px;
  color: var(--sx-primary);
  font-size: 13px;
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-desc {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 5px;
  color: var(--sx-body);
  font-size: 13px;
  line-height: 21px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.history-meta,
.history-time {
  display: block;
  overflow: hidden;
  margin-top: 4px;
  color: var(--sx-muted);
  font-size: 12px;
  line-height: 19px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-history {
  position: absolute;
  top: 9px;
  right: 9px;
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
  .history-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .history-grid {
    grid-template-columns: 1fr;
  }
}
</style>
