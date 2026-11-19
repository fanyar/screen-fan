<template>
  <section v-if="items.length" class="platform-related">
    <h2 class="related-title">相关推荐</h2>
    <div class="related-grid">
      <button
          v-for="item in items"
          :key="`${item.contentType}-${item.id}`"
          type="button"
          class="related-card"
          @click="navigate(item)"
      >
        <div class="related-cover">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy">
          <span v-else class="related-cover__fallback">{{ item.title }}</span>
        </div>
        <span class="related-name">{{ item.title }}</span>
      </button>
    </div>
  </section>
</template>

<script setup>
import {useRouter} from 'vue-router'
import {useRoute} from 'vue-router'
import {preservedListRouteQuery} from '@/util/list-query'

const route = useRoute()

defineProps({
  items: {type: Array, default: () => []}
})

const router = useRouter()
const basePath = {
  works: '/platform/works',
  comics: '/platform/comics',
  movies: '/platform/movies',
  anime: '/platform/anime',
  articles: '/platform/articles'
}

const navigate = (item) => {
  const base = basePath[item.contentType]
  if (base) {
    const sameModule = route.path.startsWith(base)
    router.push({
      path: `${base}/${item.id}`,
      query: sameModule ? preservedListRouteQuery(route.query) : {}
    })
  }
}
</script>

<style scoped lang="scss">
.platform-related {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--sx-line);
}

.related-title {
  margin: 0 0 16px;
  color: var(--sx-title);
  font-size: 20px;
  line-height: 28px;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}

.related-card {
  padding: 0;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s ease, transform 0.15s ease;

  &:hover {
    border-color: var(--sx-primary);
    transform: translateY(-2px);
  }
}

.related-cover {
  aspect-ratio: 16 / 9;
  border-radius: 6px 6px 0 0;
  overflow: hidden;

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.related-cover__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 12px;
  background: var(--sx-soft);
  color: var(--sx-muted);
  font-size: 14px;
}

.related-name {
  display: block;
  padding: 10px 12px;
  color: var(--sx-title);
  font-size: 14px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
