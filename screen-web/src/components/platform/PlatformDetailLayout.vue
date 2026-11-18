<template>
  <article class="detail-panel" :class="`detail-panel--${aspect}`">
    <div class="detail-cover" :class="`detail-cover--${aspect}`">
      <img v-if="cover" :src="cover" :alt="title">
      <div v-else class="platform-cover__fallback">
        <span>{{ title }}</span>
      </div>
    </div>

    <div class="detail-content">
      <div class="detail-actions">
        <button class="detail-back" type="button" @click="emit('back')">
          <el-icon><ArrowLeft/></el-icon>
          <span>返回列表</span>
        </button>
        <div class="detail-action-group">
          <slot name="actions"/>
          <PlatformShareButton :title="title"/>
        </div>
      </div>
      <header class="detail-header">
        <div class="detail-title-row">
          <h1 class="detail-title">{{ title }}</h1>
          <div v-if="slots['title-actions']" class="detail-title-actions">
            <slot name="title-actions"/>
          </div>
        </div>
        <div v-if="meta.length" class="detail-meta">
          <span v-for="item in meta" :key="item">{{ item }}</span>
        </div>
        <p v-if="description" class="detail-description">{{ description }}</p>
        <div v-if="tags.length" class="detail-tags">
          <span v-for="tag in tags" :key="tag" class="platform-tag">{{ tag }}</span>
        </div>
        <div v-if="slots['after-tags']" class="detail-after-tags">
          <slot name="after-tags"/>
        </div>
      </header>

      <section v-if="slots.default" class="detail-section scrollBar">
        <slot/>
      </section>

    </div>
  </article>
</template>

<script setup>
import {useSlots} from 'vue'
import PlatformShareButton from '@/components/platform/PlatformShareButton.vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  cover: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  tags: {
    type: Array,
    default: () => []
  },
  meta: {
    type: Array,
    default: () => []
  },
  aspect: {
    type: String,
    default: 'wide',
    validator: value => ['wide', 'poster'].includes(value)
  }
})

const emit = defineEmits(['back'])
const slots = useSlots()
</script>

<style scoped lang="scss">
.detail-panel {
  display: grid;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
  grid-template-columns: minmax(0, 42%) minmax(0, 1fr);
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
}

.detail-cover {
  height: 100%;
  overflow: hidden;
  background: var(--sx-cover-bg);

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &--poster {
    aspect-ratio: 2 / 3;
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
  padding: 32px 36px 26px;
}

.detail-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: -4px 0 12px -8px;

  :deep(.el-button) {
    height: 34px;
  }
}

.detail-action-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  height: 34px;
  padding: 0 8px;
  margin: 0;
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

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--sx-muted);
  font-size: 14px;
}

.detail-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
}

.detail-title-actions {
  flex: none;
}

.detail-title {
  flex: 1;
  min-width: 0;
  margin: 0;
  color: var(--sx-title);
  font-size: 32px;
  line-height: 44px;
}

.detail-description {
  margin: 16px 0 0;
  color: var(--sx-body);
  font-size: 16px;
  line-height: 28px;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 18px;
}

.detail-after-tags {
  margin-top: 14px;
}

.detail-section {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  margin-top: 26px;
  padding: 22px 8px 2px 0;
  border-top: 1px solid var(--sx-line);
  color: var(--sx-body);
  font-size: 16px;
  line-height: 28px;
}

@media (max-width: 900px) {
  .detail-panel {
    grid-template-columns: 1fr;
    grid-template-rows: min(38vh, 300px) minmax(0, 1fr);
  }

  .detail-cover--poster {
    aspect-ratio: auto;
    height: 100%;
  }

  .detail-content {
    padding: 24px 20px;
  }

  .detail-title {
    font-size: 26px;
    line-height: 37px;
  }
}
</style>
