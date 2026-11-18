<script setup>import { ref, onMounted, watch } from 'vue';
import { useMusicStore } from '@/store/modules/music';
import { musicApi } from '@/api/music';
const store = useMusicStore();
const audioRef = ref(null);
const progress = ref(0);
const progressInterval = ref(null);
const playAudio = () => {
 if (audioRef.value && store.currentSong) {
 audioRef.value.play().catch(err => {
 console.error('Play failed:', err);
 });
 }
};
const pauseAudio = () => {
 if (audioRef.value) {
 audioRef.value.pause();
 }
};
const togglePlay = () => {
 if (store.isPlaying) {
 pauseAudio();
 }
 else {
 playAudio();
 }
 store.togglePlay();
};
const seekTo = (event) => {
 if (audioRef.value && store.duration) {
 const percent = event.target.value / 100;
 const time = percent * store.duration / 1000;
 audioRef.value.currentTime = time;
 progress.value = percent * 100;
 }
};
const handleTimeUpdate = () => {
 if (audioRef.value && store.duration) {
 store.setCurrentTime(audioRef.value.currentTime);
 progress.value = (audioRef.value.currentTime / (store.duration / 1000)) * 100;
 }
};
const handleLoadedMetadata = () => {
 if (audioRef.value) {
 store.setDuration(audioRef.value.duration * 1000);
 }
};
const handleEnded = () => {
 store.playNext();
};
watch(() => store.currentSong, async (newSong) => {
 if (newSong && audioRef.value) {
 try {
 const res = await musicApi.getSongUrl(newSong.id);
 if (res && res.data && res.data[0] && res.data[0].url) {
 audioRef.value.src = res.data[0].url;
 newSong.url = res.data[0].url;
 }
 else {
 audioRef.value.src = '';
 }
 }
 catch (error) {
 console.error('Get song URL failed:', error);
 audioRef.value.src = '';
 }
 setTimeout(() => {
 if (store.isPlaying) {
 playAudio();
 }
 }, 100);
 }
}, { immediate: true });
watch(() => store.isPlaying, (playing) => {
 if (playing) {
 playAudio();
 }
 else {
 pauseAudio();
 }
});
onMounted(() => {
 if (store.currentSong) {
 store.isPlaying = true;
 }
});
</script>

<template>
  <div class="border-t border-gray-700 bg-gray-800 px-[24px] py-[12px] text-[14px]">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-[16px]">
        <img
          v-if="store.currentSong?.cover"
          :src="store.currentSong.cover + '?param=60y60'"
          :alt="store.currentSong.album"
          class="h-[48px] w-[48px] rounded-[8px] object-cover"
        />
        <div v-else class="flex h-[48px] w-[48px] items-center justify-center rounded-[8px] bg-gray-700 text-[18px] leading-[24px]">
          🎵
        </div>
        <div class="min-w-0">
          <p class="max-w-[200px] truncate text-[14px] font-medium leading-[22px]">{{ store.currentSong?.name || '未播放' }}</p>
          <p class="text-[14px] leading-[18px] text-gray-400">{{ store.currentSong?.artist || '未知歌手' }}</p>
        </div>
      </div>

      <div class="mx-[32px] flex max-w-[448px] flex-1 items-center gap-[16px]">
        <button
          @click="store.playPrev"
          class="text-[16px] leading-[22px] text-gray-400 transition-colors duration-200 hover:text-white"
        >
          ⏮️
        </button>
        <button
          @click="togglePlay"
          :class="[
            'flex h-[48px] w-[48px] items-center justify-center rounded-full text-[18px] leading-[24px] transition-all duration-200',
            store.isPlaying ? 'bg-purple-500 text-white' : 'bg-gray-700 text-white hover:bg-gray-600'
          ]"
        >
          {{ store.isPlaying ? '⏸️' : '▶️' }}
        </button>
        <button
          @click="store.playNext"
          class="text-[16px] leading-[22px] text-gray-400 transition-colors duration-200 hover:text-white"
        >
          ⏭️
        </button>
        
        <div class="flex flex-1 items-center gap-[12px]">
          <span class="w-[64px] text-[14px] leading-[18px] text-gray-400">{{ store.formatTime(store.currentTime) }}</span>
          <input
            type="range"
            min="0"
            max="100"
            :value="progress"
            @input="seekTo"
            class="h-[4px] flex-1 cursor-pointer appearance-none rounded-full bg-gray-600 slider"
          />
          <span class="w-[64px] text-[14px] leading-[18px] text-gray-400">{{ store.formatTime(store.duration / 1000) }}</span>
        </div>
      </div>

      <div class="flex items-center gap-[12px]">
        <input
          type="range"
          min="0"
          max="100"
          :value="store.volume * 100"
          @input="(e) => store.setVolume(e.target.value / 100)"
          class="h-[4px] w-[80px] cursor-pointer appearance-none rounded-full bg-gray-600 slider"
        />
        <span class="text-[16px] leading-[22px] text-gray-400">🔊</span>
      </div>
    </div>

    <audio
      ref="audioRef"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoadedMetadata"
      @ended="handleEnded"
    />
  </div>
</template>

<style scoped>
.slider::-webkit-slider-thumb {
  appearance: none;
  width: 12px;
  height: 12px;
  background: #a855f7;
  border-radius: 50%;
  cursor: pointer;
}

.slider::-moz-range-thumb {
  width: 12px;
  height: 12px;
  background: #a855f7;
  border-radius: 50%;
  cursor: pointer;
  border: none;
}
</style>
