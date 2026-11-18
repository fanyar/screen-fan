import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMusicStore = defineStore('music', () => {
  const currentSong = ref(null)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(0.7)
  const playlist = ref([])
  const favorites = ref([])
  const currentIndex = ref(0)

  const loadFavorites = () => {
    const saved = localStorage.getItem('music_favorites')
    if (saved) {
      favorites.value = JSON.parse(saved)
    }
  }

  const saveFavorites = () => {
    localStorage.setItem('music_favorites', JSON.stringify(favorites.value))
  }

  const playSong = (song, list = []) => {
    currentSong.value = song
    if (list.length > 0) {
      playlist.value = list
      currentIndex.value = list.findIndex(item => item.id === song.id)
    }
    isPlaying.value = true
    duration.value = song.duration || 0
    currentTime.value = 0
  }

  const togglePlay = () => {
    isPlaying.value = !isPlaying.value
  }

  const setCurrentTime = (time) => {
    currentTime.value = time
  }

  const setDuration = (time) => {
    duration.value = time
  }

  const setVolume = (vol) => {
    volume.value = vol
  }

  const playNext = () => {
    if (playlist.value.length === 0) return
    currentIndex.value = (currentIndex.value + 1) % playlist.value.length
    currentSong.value = playlist.value[currentIndex.value]
    currentTime.value = 0
  }

  const playPrev = () => {
    if (playlist.value.length === 0) return
    currentIndex.value = (currentIndex.value - 1 + playlist.value.length) % playlist.value.length
    currentSong.value = playlist.value[currentIndex.value]
    currentTime.value = 0
  }

  const addToFavorites = (song) => {
    if (!favorites.value.find(item => item.id === song.id)) {
      favorites.value.push(song)
      saveFavorites()
    }
  }

  const removeFromFavorites = (songId) => {
    favorites.value = favorites.value.filter(item => item.id !== songId)
    saveFavorites()
  }

  const isFavorite = (songId) => {
    return favorites.value.some(item => item.id === songId)
  }

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }

  return {
    currentSong,
    isPlaying,
    currentTime,
    duration,
    volume,
    playlist,
    favorites,
    currentIndex,
    playSong,
    togglePlay,
    setCurrentTime,
    setDuration,
    setVolume,
    playNext,
    playPrev,
    addToFavorites,
    removeFromFavorites,
    isFavorite,
    formatTime,
    loadFavorites
  }
})
