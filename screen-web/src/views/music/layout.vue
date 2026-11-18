<script setup>
import { useRouter, useRoute } from 'vue-router'
import Player from '@/components/music/Player.vue'

const router = useRouter()
const route = useRoute()

const navItems = [
  { name: '首页', path: '/test/music/home', icon: '🎵' },
  { name: '我的收藏', path: '/test/music/favorites', icon: '❤️' }
]

const isActive = (path) => route.path === path
</script>

<template>
  <div class="flex h-screen flex-col overflow-hidden bg-gray-900 text-[14px] text-white">
    <header class="flex items-center justify-between border-b border-gray-700 bg-gray-800 px-[24px] py-[16px]">
      <div class="flex items-center gap-[12px]">
        <div class="flex h-[40px] w-[40px] items-center justify-center rounded-[8px] bg-gradient-to-br from-purple-500 to-pink-500 text-[20px] font-bold">
          M
        </div>
        <span class="text-[20px] font-semibold leading-[28px]">MusicX</span>
      </div>
      <nav class="flex items-center gap-[24px]">
        <button
          v-for="item in navItems"
          :key="item.path"
          @click="router.push(item.path)"
          :class="[
            'flex items-center gap-[8px] rounded-[8px] px-[16px] py-[8px] text-[14px] leading-[22px] transition-all duration-200',
            isActive(item.path)
              ? 'bg-gray-700 text-white'
              : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
          ]"
        >
          <span>{{ item.icon }}</span>
          <span>{{ item.name }}</span>
        </button>
      </nav>
    </header>

    <main class="flex-1 overflow-hidden">
      <router-view />
    </main>

    <Player />
  </div>
</template>
