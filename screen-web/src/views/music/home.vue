<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { musicApi } from '@/api/music'
import { useMusicStore } from '@/store/modules/music'

const router = useRouter()
const store = useMusicStore()

const hotSongs = ref([])
const newSongs = ref([])
const topSongs = ref([])
const classicSongs = ref([])
const loading = ref(true)

const formatDuration = (ms) => {
  const seconds = Math.floor(ms / 1000)
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const playSong = (song, list) => {
  store.playSong({
    id: song.id,
    name: song.name,
    artist: song.ar?.map(a => a.name).join('/') || '未知歌手',
    album: song.al?.name || '未知专辑',
    cover: song.al?.picUrl || '',
    duration: song.dt || 0,
    url: ''
  }, list.map(s => ({
    id: s.id,
    name: s.name,
    artist: s.ar?.map(a => a.name).join('/') || '未知歌手',
    album: s.al?.name || '未知专辑',
    cover: s.al?.picUrl || '',
    duration: s.dt || 0,
    url: ''
  })))
}

const toggleFavorite = (song, event) => {
  event.stopPropagation()
  if (store.isFavorite(song.id)) {
    store.removeFromFavorites(song.id)
  } else {
    store.addToFavorites({
      id: song.id,
      name: song.name,
      artist: song.ar?.map(a => a.name).join('/') || '未知歌手',
      album: song.al?.name || '未知专辑',
      cover: song.al?.picUrl || '',
      duration: song.dt || 0,
      url: ''
    })
  }
}

const mockSongs = [
  { id: 1, name: '晴天', ar: [{ name: '周杰伦' }], al: { name: '叶惠美', picUrl: 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/18697237379332211.jpg' }, dt: 269000 },
  { id: 2, name: '七里香', ar: [{ name: '周杰伦' }], al: { name: '七里香', picUrl: 'https://p2.music.126.net/1zHtl6-0j4L86Ff0q6lQ1Q==/18697237379332212.jpg' }, dt: 299000 },
  { id: 3, name: '夜曲', ar: [{ name: '周杰伦' }], al: { name: '十一月的萧邦', picUrl: 'https://p2.music.126.net/9xQ8P6YF9-7K1u6S7e0u2Q==/18697237379332213.jpg' }, dt: 268000 },
  { id: 4, name: '稻香', ar: [{ name: '周杰伦' }], al: { name: '魔杰座', picUrl: 'https://p2.music.126.net/aX2L6-4z6-8K1u5S6e0u1Q==/18697237379332214.jpg' }, dt: 223000 },
  { id: 5, name: '告白气球', ar: [{ name: '周杰伦' }], al: { name: '周杰伦的床边故事', picUrl: 'https://p2.music.126.net/bX2L7-5z7-9K2u6S7e1u2Q==/18697237379332215.jpg' }, dt: 215000 },
  { id: 6, name: '说好不哭', ar: [{ name: '周杰伦' }], al: { name: '说好不哭', picUrl: 'https://p2.music.126.net/cX2L8-6z8-0K3u7S8e2u3Q==/18697237379332216.jpg' }, dt: 246000 },
  { id: 7, name: 'Mojito', ar: [{ name: '周杰伦' }], al: { name: 'Mojito', picUrl: 'https://p2.music.126.net/dX2L9-7z9-1K4u8S9e3u4Q==/18697237379332217.jpg' }, dt: 222000 },
  { id: 8, name: '最伟大的作品', ar: [{ name: '周杰伦' }], al: { name: '最伟大的作品', picUrl: 'https://p2.music.126.net/eX2L0-8z0-2K5u9S0e4u5Q==/18697237379332218.jpg' }, dt: 263000 },
  { id: 9, name: '青花瓷', ar: [{ name: '周杰伦' }], al: { name: '我很忙', picUrl: 'https://p2.music.126.net/fX2L1-9z1-3K6u0S1e5u6Q==/18697237379332219.jpg' }, dt: 295000 },
  { id: 10, name: '简单爱', ar: [{ name: '周杰伦' }], al: { name: '范特西', picUrl: 'https://p2.music.126.net/gX2L2-0z2-4K7u1S2e6u7Q==/18697237379332220.jpg' }, dt: 276000 }
]

const mockClassicSongs = [
  { id: 186345, name: '爱如潮水', ar: [{ name: '张信哲' }], al: { name: '心事', picUrl: 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/18697237379332211.jpg' }, dt: 272000 },
  { id: 254574, name: '后来', ar: [{ name: '刘若英' }], al: { name: '我等你', picUrl: 'https://p2.music.126.net/1zHtl6-0j4L86Ff0q6lQ1Q==/18697237379332212.jpg' }, dt: 341000 },
  { id: 346089, name: '海阔天空', ar: [{ name: 'Beyond' }], al: { name: '命运派对', picUrl: 'https://p2.music.126.net/9xQ8P6YF9-7K1u6S7e0u2Q==/18697237379332213.jpg' }, dt: 322000 },
  { id: 346576, name: '光辉岁月', ar: [{ name: 'Beyond' }], al: { name: '命运派对', picUrl: 'https://p2.music.126.net/aX2L6-4z6-8K1u5S6e0u1Q==/18697237379332214.jpg' }, dt: 298000 },
  { id: 287035, name: '遇见', ar: [{ name: '孙燕姿' }], al: { name: 'My Story Your Song', picUrl: 'https://p2.music.126.net/bX2L7-5z7-9K2u6S7e1u2Q==/18697237379332215.jpg' }, dt: 210000 },
  { id: 212233, name: '千千阙歌', ar: [{ name: '陈慧娴' }], al: { name: '千千阙歌', picUrl: 'https://p2.music.126.net/cX2L8-6z8-0K3u7S8e2u3Q==/18697237379332216.jpg' }, dt: 299000 },
  { id: 4876355, name: '至少还有你', ar: [{ name: '林忆莲' }], al: { name: '爱的主题曲', picUrl: 'https://p2.music.126.net/dX2L9-7z9-1K4u8S9e3u4Q==/18697237379332217.jpg' }, dt: 274000 },
  { id: 190381, name: '水手', ar: [{ name: '郑智化' }], al: { name: '私房歌', picUrl: 'https://p2.music.126.net/eX2L0-8z0-2K5u9S0e4u5Q==/18697237379332218.jpg' }, dt: 297000 },
  { id: 186901, name: '吻别', ar: [{ name: '张学友' }], al: { name: '吻别', picUrl: 'https://p2.music.126.net/fX2L1-9z1-3K6u0S1e5u6Q==/18697237379332219.jpg' }, dt: 283000 },
  { id: 186389, name: '朋友', ar: [{ name: '周华健' }], al: { name: '朋友', picUrl: 'https://p2.music.126.net/gX2L2-0z2-4K7u1S2e6u7Q==/18697237379332220.jpg' }, dt: 290000 }
]

onMounted(async () => {
  store.loadFavorites()
  try {
    const [hotRes, newRes, topRes, classicRes] = await Promise.all([
      musicApi.getHotSongs(),
      musicApi.getNewSongs(),
      musicApi.getTopList(),
      musicApi.getPlaylistDetail(14289009022)
    ])

    if (hotRes && hotRes.playlist && hotRes.playlist.tracks) {
      hotSongs.value = hotRes.playlist.tracks.slice(0, 10)
    } else {
      hotSongs.value = mockSongs
    }
    if (newRes && newRes.playlist && newRes.playlist.tracks) {
      newSongs.value = newRes.playlist.tracks.slice(0, 10)
    } else {
      newSongs.value = mockSongs.slice(0, 8)
    }
    if (topRes && topRes.playlist && topRes.playlist.tracks) {
      topSongs.value = topRes.playlist.tracks.slice(0, 10)
    } else {
      topSongs.value = mockSongs.slice(0, 6)
    }
    if (classicRes && classicRes.playlist && classicRes.playlist.tracks) {
      classicSongs.value = classicRes.playlist.tracks.slice(0, 10)
    } else {
      classicSongs.value = mockClassicSongs
    }
  } catch (error) {
    console.error('Load music data failed, using mock data:', error)
    hotSongs.value = mockSongs
    newSongs.value = mockSongs.slice(0, 8)
    topSongs.value = mockSongs.slice(0, 6)
    classicSongs.value = mockClassicSongs
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="flex h-full flex-col p-[24px] text-[14px]">
    <div class="mb-[16px]">
      <h1 class="mb-[8px] text-[30px] font-bold leading-[38px]">发现音乐</h1>
      <p class="text-[14px] leading-[22px] text-gray-400">探索热门歌曲、新歌上架、排行榜和经典老歌</p>
    </div>

    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="h-[40px] w-[40px] animate-spin rounded-full border-[4px] border-purple-500 border-t-transparent"></div>
    </div>

    <div v-else class="grid min-h-0 flex-1 grid-cols-1 gap-[24px] overflow-hidden md:grid-cols-2 lg:grid-cols-4">
      <div class="flex h-full min-h-0 flex-col rounded-[8px] bg-gray-800 p-[20px]">
        <div class="mb-[16px] flex items-center gap-[8px]">
          <span class="text-[24px] leading-[32px]">🔥</span>
          <h2 class="text-[20px] font-semibold leading-[28px]">热门歌曲</h2>
        </div>
        <ul class="flex flex-1 flex-col gap-[12px] overflow-y-auto scrollBar">
          <li
            v-for="(song, index) in hotSongs"
            :key="song.id"
            @click="playSong(song, hotSongs)"
            :class="[
              'flex cursor-pointer items-center gap-[12px] rounded-[8px] p-[12px] transition-all duration-200',
              index < 3 ? 'bg-gray-700/50' : 'hover:bg-gray-700/30'
            ]"
          >
            <span :class="[
              'flex h-[24px] w-[24px] shrink-0 items-center justify-center rounded-full text-[14px] font-bold leading-[18px]',
              index === 0 ? 'bg-yellow-500 text-black' :
              index === 1 ? 'bg-gray-400 text-black' :
              index === 2 ? 'bg-amber-600 text-white' :
              'text-gray-500'
            ]">
              {{ index + 1 }}
            </span>
            <img
              v-if="song.al?.picUrl"
              :src="song.al.picUrl + '?param=40y40'"
              :alt="song.al.name"
              class="h-[40px] w-[40px] shrink-0 rounded-[8px] object-cover"
            />
            <div class="min-w-0 flex-1">
              <p class="truncate text-[14px] font-medium leading-[22px]">{{ song.name }}</p>
              <p class="truncate text-[14px] leading-[18px] text-gray-400">{{ song.ar?.map(a => a.name).join('/') }}</p>
            </div>
            <span class="text-[14px] leading-[18px] text-gray-500">{{ formatDuration(song.dt) }}</span>
            <button
              @click="toggleFavorite(song, $event)"
              :class="[
                'rounded-[8px] p-[4px] text-[14px] leading-[20px] transition-all duration-200',
                store.isFavorite(song.id)
                  ? 'text-red-500 hover:text-red-400'
                  : 'text-gray-500 hover:text-red-500'
              ]"
            >
              {{ store.isFavorite(song.id) ? '❤️' : '🤍' }}
            </button>
          </li>
        </ul>
      </div>

      <div class="flex h-full min-h-0 flex-col rounded-[8px] bg-gray-800 p-[20px]">
        <div class="mb-[16px] flex items-center gap-[8px]">
          <span class="text-[24px] leading-[32px]">🆕</span>
          <h2 class="text-[20px] font-semibold leading-[28px]">新歌上架</h2>
        </div>
        <ul class="flex flex-1 flex-col gap-[12px] overflow-y-auto scrollBar">
          <li
            v-for="(song, index) in newSongs"
            :key="song.id"
            @click="playSong(song, newSongs)"
            :class="[
              'flex cursor-pointer items-center gap-[12px] rounded-[8px] p-[12px] transition-all duration-200',
              index < 3 ? 'bg-gray-700/50' : 'hover:bg-gray-700/30'
            ]"
          >
            <span :class="[
              'flex h-[24px] w-[24px] shrink-0 items-center justify-center rounded-full text-[14px] font-bold leading-[18px]',
              index === 0 ? 'bg-green-500 text-black' :
              index === 1 ? 'bg-gray-400 text-black' :
              index === 2 ? 'bg-amber-600 text-white' :
              'text-gray-500'
            ]">
              {{ index + 1 }}
            </span>
            <img
              v-if="song.al?.picUrl"
              :src="song.al.picUrl + '?param=40y40'"
              :alt="song.al.name"
              class="h-[40px] w-[40px] shrink-0 rounded-[8px] object-cover"
            />
            <div class="min-w-0 flex-1">
              <p class="truncate text-[14px] font-medium leading-[22px]">{{ song.name }}</p>
              <p class="truncate text-[14px] leading-[18px] text-gray-400">{{ song.ar?.map(a => a.name).join('/') }}</p>
            </div>
            <span class="text-[14px] leading-[18px] text-gray-500">{{ formatDuration(song.dt) }}</span>
            <button
              @click="toggleFavorite(song, $event)"
              :class="[
                'rounded-[8px] p-[4px] text-[14px] leading-[20px] transition-all duration-200',
                store.isFavorite(song.id)
                  ? 'text-red-500 hover:text-red-400'
                  : 'text-gray-500 hover:text-red-500'
              ]"
            >
              {{ store.isFavorite(song.id) ? '❤️' : '🤍' }}
            </button>
          </li>
        </ul>
      </div>

      <div class="flex h-full min-h-0 flex-col rounded-[8px] bg-gray-800 p-[20px]">
        <div class="mb-[16px] flex items-center gap-[8px]">
          <span class="text-[24px] leading-[32px]">📊</span>
          <h2 class="text-[20px] font-semibold leading-[28px]">排行榜</h2>
        </div>
        <ul class="flex flex-1 flex-col gap-[12px] overflow-y-auto scrollBar">
          <li
            v-for="(song, index) in topSongs"
            :key="song.id"
            @click="playSong(song, topSongs)"
            :class="[
              'flex cursor-pointer items-center gap-[12px] rounded-[8px] p-[12px] transition-all duration-200',
              index < 3 ? 'bg-gray-700/50' : 'hover:bg-gray-700/30'
            ]"
          >
            <span :class="[
              'flex h-[24px] w-[24px] shrink-0 items-center justify-center rounded-full text-[14px] font-bold leading-[18px]',
              index === 0 ? 'bg-purple-500 text-white' :
              index === 1 ? 'bg-gray-400 text-black' :
              index === 2 ? 'bg-amber-600 text-white' :
              'text-gray-500'
            ]">
              {{ index + 1 }}
            </span>
            <img
              v-if="song.al?.picUrl"
              :src="song.al.picUrl + '?param=40y40'"
              :alt="song.al.name"
              class="h-[40px] w-[40px] shrink-0 rounded-[8px] object-cover"
            />
            <div class="min-w-0 flex-1">
              <p class="truncate text-[14px] font-medium leading-[22px]">{{ song.name }}</p>
              <p class="truncate text-[14px] leading-[18px] text-gray-400">{{ song.ar?.map(a => a.name).join('/') }}</p>
            </div>
            <span class="text-[14px] leading-[18px] text-gray-500">{{ formatDuration(song.dt) }}</span>
            <button
              @click="toggleFavorite(song, $event)"
              :class="[
                'rounded-[8px] p-[4px] text-[14px] leading-[20px] transition-all duration-200',
                store.isFavorite(song.id)
                  ? 'text-red-500 hover:text-red-400'
                  : 'text-gray-500 hover:text-red-500'
              ]"
            >
              {{ store.isFavorite(song.id) ? '❤️' : '🤍' }}
            </button>
          </li>
        </ul>
      </div>

      <div class="flex h-full min-h-0 flex-col rounded-[8px] bg-gray-800 p-[20px]">
        <div class="mb-[16px] flex items-center gap-[8px]">
          <span class="text-[24px] leading-[32px]">🎞️</span>
          <h2 class="text-[20px] font-semibold leading-[28px]">8090经典</h2>
        </div>
        <ul class="flex flex-1 flex-col gap-[12px] overflow-y-auto scrollBar">
          <li
            v-for="(song, index) in classicSongs"
            :key="song.id"
            @click="playSong(song, classicSongs)"
            :class="[
              'flex cursor-pointer items-center gap-[12px] rounded-[8px] p-[12px] transition-all duration-200',
              index < 3 ? 'bg-gray-700/50' : 'hover:bg-gray-700/30'
            ]"
          >
            <span :class="[
              'flex h-[24px] w-[24px] shrink-0 items-center justify-center rounded-full text-[14px] font-bold leading-[18px]',
              index === 0 ? 'bg-amber-600 text-white' :
              index === 1 ? 'bg-gray-400 text-black' :
              index === 2 ? 'bg-amber-800 text-white' :
              'text-gray-500'
            ]">
              {{ index + 1 }}
            </span>
            <img
              v-if="song.al?.picUrl"
              :src="song.al.picUrl + '?param=40y40'"
              :alt="song.al.name"
              class="h-[40px] w-[40px] shrink-0 rounded-[8px] object-cover"
            />
            <div class="min-w-0 flex-1">
              <p class="truncate text-[14px] font-medium leading-[22px]">{{ song.name }}</p>
              <p class="truncate text-[14px] leading-[18px] text-gray-400">{{ song.ar?.map(a => a.name).join('/') }}</p>
            </div>
            <span class="text-[14px] leading-[18px] text-gray-500">{{ formatDuration(song.dt) }}</span>
            <button
              @click="toggleFavorite(song, $event)"
              :class="[
                'rounded-[8px] p-[4px] text-[14px] leading-[20px] transition-all duration-200',
                store.isFavorite(song.id)
                  ? 'text-red-500 hover:text-red-400'
                  : 'text-gray-500 hover:text-red-500'
              ]"
            >
              {{ store.isFavorite(song.id) ? '❤️' : '🤍' }}
            </button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
