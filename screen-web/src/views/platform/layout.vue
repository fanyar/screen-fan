<template>
  <div class="flex h-screen w-full flex-col overflow-hidden bg-sx-canvas text-[16px] text-sx-body">
    <header class="shrink-0 bg-sx-surface">
      <div class="mx-auto flex h-[56px] w-full max-w-[1440px] items-center justify-between px-[24px]">
        <button class="flex items-center gap-[10px] text-sx-primary" @click="$router.push('/home')">
          <svg class="h-[28px]" viewBox="0 0 160 48" role="img" aria-label="FAN logo">
            <g fill="currentColor">
              <path d="M2 4h34v10H12v7h22v10H12v13H2z"/>
              <path d="M52 4h10l22 40H72L62 21l-8 23H42z"/>
              <rect x="62" y="28" width="46" height="8"/>
              <path d="M120 44V4h10l26 23V4h10v40h-10L130 21v23z"/>
            </g>
          </svg>
          <span class="text-[18px] font-semibold text-sx-title">{{ platformName }}</span>
        </button>
        <div class="flex shrink-0 items-center gap-[8px]">
          <PlatformSearch/>
          <button
              type="button"
              class="header-profile"
              aria-label="个人中心"
              @click="$router.push('/platform/profile')"
          >
            <el-icon><User/></el-icon>
            <span>我的</span>
          </button>
          <button
              type="button"
              class="header-favorite"
              aria-label="我的收藏"
              @click="$router.push('/platform/favorites')"
          >
            <el-icon><Star/></el-icon>
            <span>收藏</span>
          </button>
          <button
              v-if="showDarkModeToggle"
              type="button"
              class="header-dark-toggle"
              :aria-label="themeStore.isDark ? '切换亮色' : '切换暗色'"
              @click="themeStore.toggleDarkMode()"
          >
            <el-icon><Moon v-if="!themeStore.isDark"/><Sunny v-else/></el-icon>
          </button>
          <el-dropdown trigger="click" @command="themeStore.setTheme">
            <button
                type="button"
                class="flex h-[34px] items-center rounded-[4px] px-[8px] text-[15px] text-sx-primary transition-colors hover:bg-sx-soft hover:text-sx-primary"
            >
              <span>换肤</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                    v-for="theme in platformThemes"
                    :key="theme.id"
                    :command="theme.id"
                >
                  <span class="flex items-center gap-[8px]">
                    <span class="h-[10px] w-[10px] rounded-full" :style="{background: theme.color}"/>
                    <span>{{ theme.name }}</span>
                    <el-icon v-if="themeStore.currentThemeId === theme.id" class="text-sx-primary">
                      <Check/>
                    </el-icon>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <template v-if="userStore.isLoggedIn">
            <span class="max-w-[96px] truncate text-[15px] text-sx-title">
              {{ userStore.userInfo.name }}
            </span>
            <button
                class="h-[34px] rounded-[4px] px-[8px] text-[15px] text-sx-muted transition-colors hover:bg-sx-soft hover:text-sx-primary"
                @click="handleLogout"
            >
              退出
            </button>
          </template>
          <template v-else>
            <button
                class="h-[34px] rounded-[4px] px-[10px] text-[15px] text-sx-muted transition-colors hover:bg-sx-soft hover:text-sx-primary"
                @click="openAuth(PLATFORM_AUTH_MODES.LOGIN)"
            >
              登录
            </button>
            <button
                class="h-[34px] rounded-[4px] bg-sx-primary px-[12px] text-[15px] text-white transition-colors hover:bg-sx-primary-hover"
                @click="openAuth(PLATFORM_AUTH_MODES.REGISTER)"
            >
              注册
            </button>
          </template>
        </div>
      </div>
      <nav class="border-b border-sx-line bg-sx-nav-bg">
        <div class="scrollBar mx-auto flex w-full max-w-[1440px] items-center gap-[6px] overflow-x-auto px-[24px] py-[8px]">
          <button v-for="item in menu" :key="item.path"
                  class="h-[40px] shrink-0 rounded-[8px] px-[16px] text-[17px] transition-colors"
                  :class="isMenuActive(item.path)
                    ? 'bg-sx-soft font-medium text-sx-primary'
                    : 'text-sx-muted hover:bg-sx-canvas hover:text-sx-title'"
                  @click="$router.push(item.path)">
            {{ item.name }}
          </button>
        </div>
      </nav>
    </header>
    <main class="platform-main min-h-0 flex-1 overflow-y-auto bg-sx-canvas scrollBar">
      <router-view></router-view>
    </main>
  </div>
  <PlatformAuthDialog
      v-model="authVisible"
      :mode="authMode"
      @mode-change="authMode = $event"
  />
</template>

<script setup>
import {ref} from 'vue'
import {ElMessage} from 'element-plus'
import {
  PLATFORM_AUTH_MODES,
  PLATFORM_MENU,
  PLATFORM_NAME,
  PLATFORM_SHOW_DARK_MODE_TOGGLE,
  PLATFORM_THEMES
} from '@/const/platform'
import {useRoute} from 'vue-router'
import {useThemeStore} from '@/store/modules/theme'
import {useUserStore} from '@/store/modules/user'
import PlatformAuthDialog from '@/components/platform/PlatformAuthDialog.vue'
import PlatformSearch from '@/components/platform/PlatformSearch.vue'
let menu = PLATFORM_MENU
let showDarkModeToggle = PLATFORM_SHOW_DARK_MODE_TOGGLE
let route = useRoute()
let platformThemes = PLATFORM_THEMES
let themeStore = useThemeStore()
let userStore = useUserStore()
let authVisible = ref(false)
let authMode = ref(PLATFORM_AUTH_MODES.LOGIN)

const isMenuActive = (path) => route.path === path || route.path.startsWith(`${path}/`)

const openAuth = (mode) => {
  authMode.value = mode
  authVisible.value = true
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
}
</script>

<style scoped lang="scss">
.header-profile,
.header-favorite {
  position: relative;
  display: flex;
  align-items: center;
  gap: 5px;
  height: 34px;
  padding: 0 10px;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--sx-muted);
  font-size: 15px;
  cursor: pointer;
  transition: color .2s ease, background-color .2s ease;

  &:hover {
    background: var(--sx-soft);
    color: var(--sx-primary);
  }
}

.header-dark-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--sx-muted);
  cursor: pointer;
  font-size: 16px;
  transition: color .2s ease, background-color .2s ease;

  &:hover {
    background: var(--sx-soft);
    color: var(--sx-primary);
  }
}

.scrollBar {
  --scrollbar-thumb-bg: var(--sx-primary);
  --scrollbar-thumb-hover: var(--sx-primary-hover);
  --scrollbar-track-bg: var(--sx-primary-soft);
}

@media (max-width: 720px) {
  .header-profile span,
  .header-favorite span {
    display: none;
  }

  .header-profile,
  .header-favorite {
    width: 34px;
    justify-content: center;
    padding: 0;
  }
}

</style>
