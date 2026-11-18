import {computed, ref} from 'vue'
import {defineStore} from 'pinia'
import {getStore, setStore} from '@/util/store'
import {
  PLATFORM_DEFAULT_THEME,
  PLATFORM_DARK_MODE_ATTRIBUTE,
  PLATFORM_DARK_MODE_STORAGE_KEY,
  PLATFORM_THEME_ATTRIBUTE,
  PLATFORM_THEME_STORAGE_KEY,
  PLATFORM_THEMES
} from '@/const/platform'

const getValidTheme = (themeId) => {
  return PLATFORM_THEMES.some(theme => theme.id === themeId)
    ? themeId
    : PLATFORM_DEFAULT_THEME
}

export const useThemeStore = defineStore('theme', () => {
  const currentThemeId = ref(getValidTheme(getStore({name: PLATFORM_THEME_STORAGE_KEY})))
  const isDark = ref(getStore({name: PLATFORM_DARK_MODE_STORAGE_KEY}) === 'dark')
  const currentTheme = computed(() => {
    return PLATFORM_THEMES.find(theme => theme.id === currentThemeId.value)
  })

  const setTheme = (themeId) => {
    const theme = PLATFORM_THEMES.find(item => item.id === themeId)
    if (!theme) return

    currentThemeId.value = theme.id
    document.documentElement.setAttribute(PLATFORM_THEME_ATTRIBUTE, theme.id)
    setStore({name: PLATFORM_THEME_STORAGE_KEY, content: theme.id})
  }

  const setDarkMode = (dark) => {
    isDark.value = dark
    document.documentElement.setAttribute(PLATFORM_DARK_MODE_ATTRIBUTE, dark ? 'dark' : 'light')
    setStore({name: PLATFORM_DARK_MODE_STORAGE_KEY, content: dark ? 'dark' : 'light'})
  }

  const toggleDarkMode = () => {
    setDarkMode(!isDark.value)
  }

  setTheme(currentThemeId.value)
  setDarkMode(isDark.value)

  return {
    currentThemeId,
    currentTheme,
    isDark,
    setTheme,
    setDarkMode,
    toggleDarkMode
  }
})
