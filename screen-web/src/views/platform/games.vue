<template>
  <div class="platform-page">
    <header class="platform-page-header">
      <h1 class="platform-page-title">游戏</h1>
      <p class="platform-page-desc">浏览器里就能玩的 3D 小游戏</p>
    </header>

    <div class="grid grid-cols-1 gap-[20px] md:grid-cols-2 lg:grid-cols-3">
      <div
          v-for="game in games"
          :key="game.id"
          class="platform-card group cursor-pointer overflow-hidden rounded-[6px]"
          @click="$router.push(game.path)"
      >
        <div class="platform-cover aspect-[16/9] overflow-hidden">
          <div
              class="game-cover flex h-full w-full items-center justify-center transition-transform duration-300 group-hover:scale-[1.03]"
              :style="{ background: game.gradient }"
          >
            <div class="flex flex-col items-center gap-[8px] text-white/90">
              <span class="game-icon" v-html="game.icon"></span>
              <span class="text-[20px] font-bold tracking-wide">{{ game.title }}</span>
            </div>
          </div>
          <span class="platform-badge left-[10px] top-[10px]">{{ game.badge }}</span>
        </div>
        <div class="p-[16px]">
          <div class="platform-card__title">{{ game.title }}</div>
          <p class="platform-card__desc">{{ game.desc }}</p>
          <div class="mt-[10px] flex flex-wrap items-center gap-[6px]">
            <span v-for="tag in game.tags" :key="tag" class="platform-tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {PLATFORM_GAMES} from '@/const/games'

const games = PLATFORM_GAMES
</script>

<style scoped lang="scss">
.game-cover {
  cursor: pointer;
}

.game-icon {
  display: inline-flex;
  color: rgba(255, 255, 255, 0.9);
}
</style>
