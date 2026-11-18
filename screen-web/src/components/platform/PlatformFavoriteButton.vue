<template>
  <button
      type="button"
      class="favorite-button"
      :class="[`favorite-button--${variant}`, {active}]"
      :aria-label="active ? '取消收藏' : '加入收藏'"
      :title="active ? '取消收藏' : '加入收藏'"
      @click.stop="handleToggle"
  >
    <el-icon>
      <StarFilled v-if="active"/>
      <Star v-else/>
    </el-icon>
    <span v-if="variant === 'text'">{{ active ? '已收藏' : '收藏' }}</span>
  </button>
</template>

<script setup>
import {computed} from 'vue'
import {ElMessage} from 'element-plus'
import {useFavoriteStore} from '@/store/modules/favorite'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: value => ['works', 'comics', 'movies', 'anime', 'articles'].includes(value)
  },
  item: {
    type: Object,
    required: true
  },
  variant: {
    type: String,
    default: 'icon',
    validator: value => ['icon', 'text'].includes(value)
  }
})

const favoriteStore = useFavoriteStore()
const active = computed(() => favoriteStore.isFavorite(props.type, props.item?.id))

const handleToggle = () => {
  const result = favoriteStore.toggle(props.type, props.item)
  if (result === 'added') ElMessage.success('已加入本地收藏')
  if (result === 'removed') ElMessage.success('已取消收藏')
  if (result === false) ElMessage.warning('收藏数量已达上限')
}
</script>

<style scoped lang="scss">
.favorite-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border: 1px solid var(--sx-line);
  background: rgba(255, 255, 255, .92);
  color: var(--sx-muted);
  cursor: pointer;
  transition: color .2s ease, border-color .2s ease, background-color .2s ease;
}

.favorite-button--icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 16px;
}

.favorite-button--text {
  height: 34px;
  padding: 0 12px;
  border-radius: 4px;
  font-size: 14px;
}

.favorite-button.active {
  border-color: rgba(234, 179, 8, .55);
  background: rgba(254, 249, 195, .92);
  color: #ca8a04;
}
</style>
