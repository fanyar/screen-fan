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
const games = [
  {
    id: 'kart-rider',
    title: '跑跑卡丁车',
    path: '/platform/games/kart',
    desc: 'Three.js 3D 卡丁车竞速。支持漂移过弯、加速带和计时赛，3 圈挑战最佳圈速。',
    tags: ['Three.js', '3D', '竞速', '漂移'],
    badge: '3D',
    gradient: 'linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%)',
    icon: '<svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="8" y="16" width="32" height="14" rx="3" fill="currentColor" opacity="0.9"/><rect x="12" y="12" width="24" height="6" rx="2" fill="currentColor" opacity="0.6"/><circle cx="15" cy="32" r="5" fill="#222"/><circle cx="33" cy="32" r="5" fill="#222"/><circle cx="15" cy="32" r="2.5" fill="#555"/><circle cx="33" cy="32" r="2.5" fill="#555"/><rect x="20" y="24" width="8" height="4" rx="1" fill="#fff" opacity="0.3"/></svg>'
  }
]
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
