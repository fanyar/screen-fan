> 返回主文档：[claude.md](./claude.md)

# 项目架构与目录说明

## 目录结构

```
screen-web/
├── public/                    # 静态资源目录
│   └── vite.svg              # 图标文件
├── src/                      # 源代码目录
│   ├── api/                  # API 接口层
│   │   ├── home.js           # 首页相关接口
│   │   └── music.js          # 网易云音乐 API 服务
│   ├── assets/               # 静态资源
│   │   ├── images/           # 图片资源
│   │   │   ├── menu/         # 菜单图标
│   │   │   └── login-bg.png  # 登录背景图
│   │   ├── screen/           # 大屏相关资源
│   │   │   ├── module/       # 模块图片
│   │   │   └── visible.mp4   # 视频资源
│   │   └── vue.svg           # Vue 图标
│   ├── components/           # 可复用组件
│   │   ├── VueRulerTool/     # 标尺工具组件
│   │   ├── bigScreen/        # 大屏组件
│   │   │   └── echarts/      # ECharts 图表组件
│   │   ├── cardBox/          # 卡片盒子组件
│   │   ├── HelloWorld.vue    # 默认组件
│   │   ├── music/            # 音乐模块组件
│   │   │   └── Player.vue    # 全局播放器组件
│   │   └── overView.vue      # 概览组件
│   ├── const/                # 常量定义
│   │   └── customModules.js  # 自定义模块常量
│   ├── hooks/                # 组合式函数
│   │   └── useClick.js       # 点击事件处理
│   ├── page/                 # 页面级组件（旧版）
│   │   ├── carouselMenu/     # 旋转菜单
│   │   ├── index/            # 布局页面
│   │   ├── login/            # 登录页
│   │   ├── menu/             # 菜单页
│   │   └── viewModule/       # 模块预览页
│   ├── router/               # 路由配置
│   │   └── index.js          # 路由定义与守卫
│   ├── screen-web/           # 大屏模块
│   ├── store/                # Pinia 状态管理
│   │   ├── modules/          # 状态模块
│   │   │   ├── basic.js      # 基础状态
│   │   │   ├── common.js     # 公共状态
│   │   │   ├── menu.js       # 菜单状态
│   │   │   ├── music.js      # 音乐播放状态
│   │   │   ├── screen.js     # 大屏状态
│   │   │   └── user.js       # 用户状态
│   │   └── index.js          # Pinia 入口
│   ├── style/                # 样式文件
│   │   ├── element-plus.scss # Element Plus 样式覆盖
│   │   ├── style.scss        # 全局样式
│   │   ├── tailwind.css      # Tailwind CSS 入口
│   │   └── theme.scss        # 主题变量
│   ├── util/                 # 工具函数
│   │   ├── index.js          # 通用工具
│   │   ├── request.js        # 请求封装
│   │   └── store.js          # 本地存储封装
│   ├── views/                # 视图层
│   │   ├── canvas/           # Canvas 画布页
│   │   ├── customScreen/     # 自定义大屏
│   │   │   ├── modules/      # 大屏模块组件
│   │   │   ├── bs-custom.js  # 大屏自定义逻辑
│   │   │   ├── customViewScreen.vue # 大屏预览
│   │   │   └── index.vue     # 自定义大屏编辑
│   │   ├── home/             # 首页
│   │   │   └── index.vue     # 首页视图
│   │   ├── music/            # 音乐模块
│   │   │   ├── layout.vue    # 暗黑主题布局
│   │   │   ├── home.vue      # 音乐首页
│   │   │   └── favorites.vue # 我的收藏
│   │   ├── project/          # 项目页
│   │   │   ├── modules/      # 项目子组件
│   │   │   ├── drawPoint.vue # 画点组件
│   │   │   ├── drawRect.vue  # 画矩形组件
│   │   │   ├── index.vue     # 项目列表
│   │   │   └── vue3.vue      # Vue3 测试页
│   │   ├── test/             # 测试页
│   │   │   └── layout.vue    # 布局测试页
│   │   └── welcome/          # 欢迎页
│   │       └── index.vue     # 欢迎页视图
│   ├── App.vue               # 根组件
│   └── main.js               # 应用入口
├── .gitignore                # Git 忽略配置
├── README.md                 # 项目说明
├── index.html                # HTML 入口
├── package.json              # 项目配置
├── vite.config.js            # Vite 配置
└── yarn.lock                 # yarn 锁文件
```

## 路由配置

使用 Vue Router 4，路由模式为 `createWebHashHistory()`（hash 模式）。

### 路由结构

| 路径                 | 名称           | 组件                                    | 说明           |
| -------------------- | -------------- | --------------------------------------- | -------------- |
| `/`                  | welcome        | welcome/index.vue                       | 欢迎页         |
| `/login`             | login          | page/login/index.vue                    | 登录页         |
| `/home`              | home           | views/home/index.vue                    | 首页           |
| `/viewModule`        | viewModule     | page/viewModule/index.vue               | 模块预览       |
| `/radialMenu`        | RadialMenu     | page/carouselMenu/RadialMenu.vue        | 旋转菜单       |
| `/screen/custom`     | 自定义大屏     | views/customScreen/index.vue            | 自定义大屏编辑 |
| `/screen/customView` | 预览自定义大屏 | views/customScreen/customViewScreen.vue | 大屏预览       |
| `/project/index`     | 测试           | views/project/index.vue                 | 项目列表       |
| `/project/vue3`      | vue3           | views/project/vue3.vue                  | Vue3 测试      |
| `/project/canvas`    | canvas         | views/canvas/index.vue                  | Canvas 画布    |
| `/test/layout`       | 布局           | views/test/layout.vue                   | 布局测试       |
| `/music`             | music          | views/music/layout.vue                  | 音乐模块布局   |
| `/music/home`        | 音乐首页       | views/music/home.vue                    | 音乐首页       |
| `/music/favorites`   | 我的收藏       | views/music/favorites.vue               | 收藏歌曲列表   |

### 导航守卫

- 白名单：`/login`、`/`、`/music`、`/music/home`、`/music/favorites`
- 其他路由需要登录验证（通过本地存储获取 userInfo）
- 路由切换时动态设置浏览器标题

## Pinia 状态管理

### store/index.js 导出方式

```js
import { createPinia } from "pinia";

export const pinia = createPinia();

export * from "./modules/common.js";
export * from "./modules/screen.js";
export * from "./modules/user.js";
export * from "./modules/menu.js";
export * from "./modules/basic.js";
```

### 设计思路

- 导出 `pinia` 实例保证全局唯一性
- 通过 `export *` 导出模块定义，实现按需加载
- 页面使用时 `import { useUserStore } from "@/store"`

### 状态模块

| 模块   | 文件      | 说明         |
| ------ | --------- | ------------ |
| basic  | basic.js  | 基础状态     |
| common | common.js | 公共状态     |
| menu   | menu.js   | 菜单状态     |
| music  | music.js  | 音乐播放状态 |
| screen | screen.js | 大屏状态     |
| user   | user.js   | 用户状态     |
