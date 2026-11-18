> 返回主文档：[claude.md](./claude.md)

# 音乐模块设计文档

## 概述

音乐模块是 ScreenX 项目中的一个独立功能模块，采用暗黑主题设计，提供音乐播放、收藏管理等功能。模块基于网易云音乐 API 实现数据获取，并提供 Mock 数据兜底策略。

## 路由设计

```js
{
  path: '/music',
  name: 'music',
  component: () => import('@/views/music/layout.vue'),
  redirect: '/music/home',
  children: [
    {
      path: 'home',
      name: '音乐首页',
      component: () => import('@/views/music/home.vue'),
    },
    {
      path: 'favorites',
      name: '我的收藏',
      component: () => import('@/views/music/favorites.vue'),
    }
  ]
}
```

### 路由特性

- `/music` 重定向到 `/music/home`，确保默认显示首页
- 采用懒加载模式，按需加载组件
- 导航守卫白名单：`/music`、`/music/home`、`/music/favorites`

## 暗黑主题实现

### 布局容器

```vue
<div class="bg-gray-900 text-white min-h-screen flex flex-col">
```

### 主题配色

| 元素 | 背景色 | 文字色 |
|------|--------|--------|
| 页面背景 | `bg-gray-900` | `text-white` |
| 头部/底部 | `bg-gray-800` | `text-gray-400` |
| 卡片 | `bg-gray-800` | `text-white` |
| 激活状态 | `bg-gray-700` | `text-white` |
| Logo | `bg-gradient-to-br from-purple-500 to-pink-500` | - |

## API 代理配置

### Vite 代理

```js
"/music-api": {
  target: "https://netease-cloud-music-api.vercel.app/",
  changeOrigin: true,
  rewrite: (path) => path.replace(/^\/music-api/, '')
}
```

### API 服务层

文件：`src/api/music.js`

| 方法 | 说明 |
|------|------|
| `getHotSongs()` | 获取热门歌曲列表 |
| `getNewSongs()` | 获取新歌上架列表 |
| `getTopList(id)` | 获取排行榜 |
| `getSongUrl(id)` | 获取歌曲播放链接 |
| `getSongDetail(ids)` | 获取歌曲详情 |
| `search(keywords, limit)` | 搜索歌曲 |
| `getLyric(id)` | 获取歌词 |

### Mock 数据兜底策略

当 API 请求失败或返回数据为空时，自动使用 Mock 数据：

```js
const mockSongs = [
  { id: 1, name: '晴天', ar: [{ name: '周杰伦' }], al: { name: '叶惠美', picUrl: '...' }, dt: 269000 },
  // ... 更多 Mock 数据
]
```

## 全局播放器组件

文件：`src/components/music/Player.vue`

### 功能特性

- 播放/暂停控制
- 上一首/下一首切换
- 进度条拖拽
- 音量调节
- 当前歌曲信息展示（封面、歌名、歌手）

### 音频流处理

```js
watch(() => store.currentSong, async (newSong) => {
  if (newSong) {
    const res = await musicApi.getSongUrl(newSong.id)
    if (res.data?.[0]?.url) {
      audioRef.value.src = res.data[0].url
    }
  }
})
```

## Pinia Store 设计

文件：`src/store/modules/music.js`

### 状态结构

| 状态 | 类型 | 说明 |
|------|------|------|
| `currentSong` | Object | 当前播放歌曲 |
| `isPlaying` | Boolean | 播放状态 |
| `currentTime` | Number | 当前播放时间 |
| `duration` | Number | 歌曲时长 |
| `volume` | Number | 音量（0-1） |
| `playlist` | Array | 播放列表 |
| `favorites` | Array | 收藏列表 |
| `currentIndex` | Number | 当前播放索引 |

### 核心方法

| 方法 | 说明 |
|------|------|
| `playSong(song, list)` | 播放指定歌曲 |
| `togglePlay()` | 切换播放状态 |
| `playNext()` | 下一首 |
| `playPrev()` | 上一首 |
| `addToFavorites(song)` | 添加到收藏 |
| `removeFromFavorites(songId)` | 从收藏移除 |
| `isFavorite(songId)` | 判断是否已收藏 |
| `formatTime(seconds)` | 格式化时间 |

## 收藏持久化方案

使用 localStorage 实现收藏数据持久化：

```js
const loadFavorites = () => {
  const saved = localStorage.getItem('music_favorites')
  if (saved) {
    favorites.value = JSON.parse(saved)
  }
}

const saveFavorites = () => {
  localStorage.setItem('music_favorites', JSON.stringify(favorites.value))
}
```

## 文件结构

```
src/
├── views/music/
│   ├── layout.vue      # 暗黑主题布局 + 导航
│   ├── home.vue        # 三栏首页（热门/新歌/排行榜）
│   └── favorites.vue   # 我的收藏
├── components/music/
│   └── Player.vue      # 全局播放器
├── api/
│   └── music.js        # 网易云音乐 API 服务
└── store/modules/
    └── music.js        # Pinia 播放状态管理
```

## 首页三栏布局

使用 Tailwind CSS 响应式网格实现：

```vue
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
```

| 断点 | 列数 |
|------|------|
| < 768px | 1 列 |
| 768px - 1199px | 2 列 |
| ≥ 1200px | 3 列 |

### 三栏内容

| 栏目 | 标识 | 数据来源 |
|------|------|----------|
| 热门歌曲 | 🔥 | `getHotSongs()` |
| 新歌上架 | 🆕 | `getNewSongs()` |
| 排行榜 | 📊 | `getTopList()` |
