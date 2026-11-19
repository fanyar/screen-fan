export const PLATFORM_NAME = 'FAN · 凡集'
export const PLATFORM_SHORT_NAME = '凡集'

export const PLATFORM_SHOW_DARK_MODE_TOGGLE = false
export const PLATFORM_SHOW_COMMENTS = false

export const PLATFORM_AUTH_MODES = {
  LOGIN: 'login',
  REGISTER: 'register'
}

export const PLATFORM_ARTICLE_STATUSES = {
  PUBLISHED: 'published',
  DRAFT: 'draft'
}

export const PLATFORM_ARTICLE_STATUS_LABELS = {
  [PLATFORM_ARTICLE_STATUSES.PUBLISHED]: '已发布',
  [PLATFORM_ARTICLE_STATUSES.DRAFT]: '草稿'
}

export const PLATFORM_THEME_ATTRIBUTE = 'data-platform-theme'
export const PLATFORM_THEME_STORAGE_KEY = 'platform-theme'
export const PLATFORM_DARK_MODE_ATTRIBUTE = 'data-mode'
export const PLATFORM_DARK_MODE_STORAGE_KEY = 'platform-dark-mode'

export const PLATFORM_THEMES = [
  {id: 'blue', name: '科技蓝', color: '#2563eb'},
  {id: 'violet', name: '星夜紫', color: '#7c3aed'},
  {id: 'emerald', name: '松林绿', color: '#059669'},
  {id: 'sunset', name: '落日橙', color: '#ea580c'}
]

export const PLATFORM_DEFAULT_THEME = PLATFORM_THEMES[0].id

export const PLATFORM_LIST_PAGE_SIZES = [6, 12, 24, 48]

export const PLATFORM_MENU = [
  {name: '电影', path: '/platform/movies'},
  {name: '动漫', path: '/platform/anime'},
  {name: '模型', path: '/platform/models'},
  {name: '游戏', path: '/platform/games'},
  {name: '作品', path: '/platform/works'},
  {name: '技术分享', path: '/platform/articles'}
]

export const PLATFORM_FAVORITE_STORAGE_KEY = 'platform-favorites'

export const PLATFORM_FAVORITE_TYPES = {
  WORKS: 'works',
  COMICS: 'comics',
  MOVIES: 'movies',
  ANIME: 'anime',
  ARTICLES: 'articles'
}

export const PLATFORM_FAVORITE_TYPE_LABELS = {
  [PLATFORM_FAVORITE_TYPES.WORKS]: '作品',
  [PLATFORM_FAVORITE_TYPES.COMICS]: '漫画',
  [PLATFORM_FAVORITE_TYPES.MOVIES]: '电影',
  [PLATFORM_FAVORITE_TYPES.ANIME]: '动漫',
  [PLATFORM_FAVORITE_TYPES.ARTICLES]: '文章'
}

export const PLATFORM_HISTORY_STORAGE_KEY = 'platform-history'
