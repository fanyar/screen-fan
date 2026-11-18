<script setup>
import { onMounted } from 'vue'
import { useMusicStore } from '@/store/modules/music'

const store = useMusicStore()

const formatDuration = (ms) => {
  const seconds = Math.floor(ms / 1000)
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const playSong = (song) => {
  store.playSong(song, store.favorites)
}

const removeFavorite = (songId) => {
  store.removeFromFavorites(songId)
}

onMounted(() => {
  store.loadFavorites()
})
</script>

<template>
  <div class="p-[24px] text-[14px]">
    <div class="mb-[32px]">
      <h1 class="mb-[8px] text-[30px] font-bold leading-[38px]">我的收藏</h1>
      <p class="text-[14px] leading-[22px] text-gray-400">收藏的歌曲列表</p>
    </div>

    <div v-if="store.favorites.length === 0" class="rounded-[8px] bg-gray-800 p-[48px] text-center">
      <div class="mb-[16px] text-[60px] leading-[68px]">🎵</div>
      <p class="text-[18px] leading-[28px] text-gray-400">还没有收藏任何歌曲</p>
      <p class="mt-[8px] text-[14px] leading-[18px] text-gray-500">去首页发现喜欢的音乐吧</p>
    </div>

    <div v-else class="overflow-hidden rounded-[8px] bg-gray-800">
      <table class="w-full">
        <thead class="bg-gray-700">
          <tr>
            <th class="px-[24px] py-[16px] text-left text-[14px] font-medium leading-[22px] text-gray-400">歌曲</th>
            <th class="px-[24px] py-[16px] text-left text-[14px] font-medium leading-[22px] text-gray-400">歌手</th>
            <th class="px-[24px] py-[16px] text-left text-[14px] font-medium leading-[22px] text-gray-400">专辑</th>
            <th class="px-[24px] py-[16px] text-left text-[14px] font-medium leading-[22px] text-gray-400">时长</th>
            <th class="px-[24px] py-[16px] text-center text-[14px] font-medium leading-[22px] text-gray-400">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="song in store.favorites"
            :key="song.id"
            @click="playSong(song)"
            class="border-b border-gray-700 hover:bg-gray-700/30 cursor-pointer transition-all duration-200"
          >
            <td class="px-[24px] py-[16px]">
              <div class="flex items-center gap-[12px]">
                <img
                  v-if="song.cover"
                  :src="song.cover + '?param=40y40'"
                  :alt="song.album"
                  class="h-[40px] w-[40px] rounded-[8px] object-cover"
                />
                <div>
                  <p class="text-[14px] font-medium leading-[22px]">{{ song.name }}</p>
                </div>
              </div>
            </td>
            <td class="px-[24px] py-[16px] text-[14px] leading-[22px] text-gray-400">{{ song.artist }}</td>
            <td class="px-[24px] py-[16px] text-[14px] leading-[22px] text-gray-400">{{ song.album }}</td>
            <td class="px-[24px] py-[16px] text-[14px] leading-[22px] text-gray-400">{{ formatDuration(song.duration) }}</td>
            <td class="px-[24px] py-[16px]">
              <button
                @click.stop="removeFavorite(song.id)"
                class="rounded-[8px] p-[8px] text-[14px] leading-[20px] text-gray-400 transition-colors duration-200 hover:bg-gray-700 hover:text-red-500"
              >
                🗑️
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
